# Document Rejection Feature Implementation

## Overview
This implementation adds a comprehensive document rejection system to the tax consultancy portal, replacing the simple boolean `is_verified` field with a more robust status-based system.

## Changes Made

### 1. Database Schema Updates

#### New Fields Added to `documents` table:
- `status` ENUM('PENDING', 'VERIFIED', 'REJECTED') - Replaces `is_verified`
- `rejected_by` BIGINT - Foreign key to users table
- `rejected_at` TIMESTAMP - When the document was rejected
- `rejection_reason` TEXT - Reason for rejection

#### Migration Script:
- `database_migration.sql` - Script to update existing database

### 2. Backend Changes

#### Entity Updates:
- `Document.java` - Added DocumentStatus enum and new fields
- `DocumentDTO.java` - Updated to include new status and rejection fields

#### Repository Updates:
- `DocumentRepository.java` - Updated queries to use status instead of is_verified
- Added new methods for status-based queries

#### Service Updates:
- `DocumentService.java` - Added rejectDocument method
- `DocumentServiceImpl.java` - Updated verification logic and added rejection logic

#### Controller Updates:
- `DocumentController.java` - Added PUT /{id}/reject endpoint

### 3. Frontend Changes

#### Service Updates:
- `DocumentService` - Added rejectDocument method and DocumentStatus enum
- Updated Document interface to use status field

#### Component Updates:
- `DocumentManagementComponent` - Added rejection modal and updated status handling
- Updated verification status display methods
- Added rejection reason input modal

#### Template Updates:
- Updated HTML templates to use new status field
- Added rejection modal with reason input
- Updated statistics cards to show rejected documents count

### 4. New Features

#### Document Status System:
- **PENDING**: Default status for new documents
- **VERIFIED**: Documents that have been approved
- **REJECTED**: Documents that have been rejected with a reason

#### Rejection Workflow:
1. User clicks "Reject" button on a pending document
2. Modal opens asking for rejection reason
3. Document status is updated to "REJECTED"
4. Rejection details are stored in database
5. Statistics are updated to reflect the change

#### Enhanced Statistics:
- Added rejected documents count to statistics
- Updated dashboard to show all three status types

## API Endpoints

### New Endpoint:
```
PUT /api/documents/{id}/reject
Parameters:
- rejectionReason (required): String explaining why document was rejected
```

### Updated Endpoint:
```
PUT /api/documents/{id}/verify
Parameters:
- isVerified (required): Boolean - true for verify, false for pending
- verifiedBy (optional): String - name of verifier
```

## Database Migration

To update an existing database:

1. Run the migration script:
```sql
source database_migration.sql
```

2. Verify the migration:
```sql
SELECT 
    COUNT(*) as total_documents,
    SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pending_documents,
    SUM(CASE WHEN status = 'VERIFIED' THEN 1 ELSE 0 END) as verified_documents,
    SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) as rejected_documents
FROM documents;
```

## Usage

### For Administrators/Staff:
1. Navigate to Document Management
2. View documents in the table
3. For pending documents, you can:
   - Click "Verify" to approve the document
   - Click "Reject" to reject with a reason
4. Rejected documents will show "Rejected" status with red styling
5. Statistics will show count of rejected documents

### Benefits:
- **Better Tracking**: Clear status for each document
- **Audit Trail**: Rejection reasons are stored and tracked
- **Improved UX**: Clear visual indicators for document status
- **Enhanced Reporting**: Statistics include rejected documents
- **Data Integrity**: Proper foreign key relationships

## Future Enhancements

1. **Email Notifications**: Send emails when documents are rejected
2. **Bulk Operations**: Allow bulk verification/rejection
3. **Status History**: Track all status changes over time
4. **Advanced Filtering**: Filter by rejection reason
5. **Export Reports**: Export documents by status

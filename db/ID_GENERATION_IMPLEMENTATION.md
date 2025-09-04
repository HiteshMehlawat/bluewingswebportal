# Sequential ID Generation Implementation

## Overview

This implementation provides automatic sequential ID generation for both **Leads** and **Service Requests** with the format:

- **Leads**: `LEAD-YYYY-NNN` (e.g., `LEAD-2025-001`, `LEAD-2025-002`)
- **Service Requests**: `SR-YYYY-NNN` (e.g., `SR-2025-001`, `SR-2025-002`)

## Features

### 1. **Year-Based Sequencing**

- Each year starts with sequence number 001
- Sequence numbers are padded to 3 digits (001, 002, ..., 999)
- Automatically detects the current year

### 2. **Automatic Generation**

- IDs are generated automatically when creating new leads or service requests
- No manual intervention required
- Thread-safe implementation using database transactions

### 3. **Database Integration**

- Uses SQL queries to find the next available sequence number
- Handles edge cases (no existing records, year transitions)
- Includes database migration for existing records

## Implementation Details

### Files Created/Modified

#### 1. **New Service Interface**

- `IdGenerationService.java` - Interface defining the ID generation methods

#### 2. **Service Implementation**

- `IdGenerationServiceImpl.java` - Implementation using JdbcTemplate for database queries

#### 3. **Entity Updates**

- `Lead.java` - Removed automatic ID generation from @PrePersist
- `ServiceRequest.java` - Removed automatic ID generation from @PrePersist

#### 4. **Service Layer Updates**

- `LeadServiceImpl.java` - Updated to use IdGenerationService
- `ServiceRequestServiceImpl.java` - Updated to use IdGenerationService

#### 5. **Database Migration**

- `id_generation_migration.sql` - SQL script to update existing records

#### 6. **Tests**

- `IdGenerationServiceTest.java` - Unit tests for the ID generation logic

### How It Works

#### 1. **Lead ID Generation**

```java
// Example: LEAD-2025-001, LEAD-2025-002, etc.
String leadId = idGenerationService.generateNextLeadId();
```

#### 2. **Service Request ID Generation**

```java
// Example: SR-2025-001, SR-2025-002, etc.
String requestId = idGenerationService.generateNextServiceRequestId();
```

#### 3. **Database Query Logic**

```sql
-- For Leads
SELECT COALESCE(MAX(CAST(SUBSTRING(lead_id, 10) AS UNSIGNED)), 0) + 1
FROM leads
WHERE lead_id LIKE 'LEAD-2025-%'

-- For Service Requests
SELECT COALESCE(MAX(CAST(SUBSTRING(request_id, 9) AS UNSIGNED)), 0) + 1
FROM service_requests
WHERE request_id LIKE 'SR-2025-%'
```

## Usage Examples

### Creating a New Lead

```java
// In LeadServiceImpl.createLead()
String leadId = idGenerationService.generateNextLeadId();
lead.setLeadId(leadId);
```

### Creating a New Service Request

```java
// In ServiceRequestServiceImpl.createServiceRequest()
String requestId = idGenerationService.generateNextServiceRequestId();
serviceRequest.setRequestId(requestId);
```

## Database Migration

### Running the Migration

1. Execute the `id_generation_migration.sql` script
2. This will update any existing records with old timestamp-based IDs
3. Verify the migration using the provided verification queries

### Migration Script Features

- Updates existing records to proper format
- Creates performance indexes
- Includes verification queries
- Safe to run multiple times

## Testing

### Unit Tests

The implementation includes comprehensive unit tests covering:

- First record of the year (001)
- Subsequent records (002, 003, etc.)
- Edge cases (null results, no existing records)
- Format validation

### Test Coverage

- ✅ Lead ID generation
- ✅ Service Request ID generation
- ✅ Year-based sequencing
- ✅ Format validation
- ✅ Edge case handling

## Benefits

### 1. **Professional Appearance**

- Clean, readable IDs (LEAD-2025-001 vs LEAD1755936558873)
- Consistent format across the application

### 2. **Easy Tracking**

- Sequential numbering makes it easy to track progress
- Year-based organization for better record management

### 3. **Scalability**

- Supports up to 999 records per year
- Can be easily extended for more digits if needed

### 4. **Maintainability**

- Centralized ID generation logic
- Easy to modify or extend for other entities

## Future Enhancements

### 1. **Multi-Year Support**

- Automatic year transition handling
- Historical data preservation

### 2. **Custom Prefixes**

- Configurable prefixes for different entity types
- Environment-specific prefixes

### 3. **Performance Optimization**

- Caching for high-frequency operations
- Batch ID generation for bulk operations

## Deployment Notes

### 1. **Database Requirements**

- MySQL 5.7+ for REGEXP support
- Proper indexes for performance

### 2. **Application Changes**

- Update existing code to use new ID generation
- Test thoroughly in development environment

### 3. **Data Migration**

- Run migration script before deploying new code
- Verify existing data integrity

## Troubleshooting

### Common Issues

#### 1. **Duplicate IDs**

- Ensure proper transaction isolation
- Check for concurrent access patterns

#### 2. **Performance Issues**

- Verify database indexes are created
- Monitor query execution plans

#### 3. **Format Issues**

- Validate ID format using regex patterns
- Check database constraints

### Debugging

- Enable debug logging for ID generation
- Monitor database queries
- Verify transaction boundaries

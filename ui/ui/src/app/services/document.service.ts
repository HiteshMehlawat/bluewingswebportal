import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';

export interface Document {
  id: number;
  taskId?: number;
  clientId: number;
  clientName: string;
  uploadedById: number;
  uploadedByName: string;
  fileName: string;
  originalFileName: string;
  filePath: string;
  fileSize: number;
  fileType: string;
  documentType: DocumentType;
  status: DocumentStatus;
  verifiedById?: number;
  verifiedByName?: string;
  verifiedAt?: string;
  rejectedById?: number;
  rejectedByName?: string;
  rejectedAt?: string;
  rejectionReason?: string;
  uploadDate: string;
  createdById?: number;
  createdByName?: string;
  updatedById?: number;
  updatedByName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface DocumentStatistics {
  totalDocuments: number;
  verifiedDocuments: number;
  pendingDocuments: number;
  rejectedDocuments: number;
  documentsUploadedToday: number;
  documentsUploadedThisWeek: number;
  documentsUploadedThisMonth: number;
  totalFileSize: number;
  averageFileSize: number;
}

export enum DocumentType {
  PAN_CARD = 'PAN_CARD',
  AADHAR = 'AADHAR',
  BANK_STATEMENT = 'BANK_STATEMENT',
  INVOICE = 'INVOICE',
  FORM_16 = 'FORM_16',
  GST_RETURN = 'GST_RETURN',
  COMPANY_DOCS = 'COMPANY_DOCS',
  OTHER = 'OTHER',
}

export enum DocumentStatus {
  PENDING = 'PENDING',
  VERIFIED = 'VERIFIED',
  REJECTED = 'REJECTED',
}

export interface DocumentFilters {
  clientId?: string;
  taskId?: string;
  documentType?: string;
  isVerified?: string;
  searchTerm?: string;
  page?: number;
  size?: number;
}

@Injectable({
  providedIn: 'root',
})
export class DocumentService {
  constructor(private http: HttpClient) {}

  // Upload document
  uploadDocument(
    file: File,
    clientId: number,
    documentType: string,
    taskId?: number
  ): Observable<Document> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('clientId', clientId.toString());
    formData.append('documentType', documentType);
    if (taskId) {
      formData.append('taskId', taskId.toString());
    }

    return this.http.post<Document>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/upload`,
      formData
    );
  }

  // Get document by ID
  getDocumentById(id: number): Observable<Document> {
    return this.http.get<Document>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/${id}`
    );
  }

  // Get documents by client
  getDocumentsByClient(clientId: number): Observable<Document[]> {
    return this.http.get<Document[]>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/client/${clientId}`
    );
  }

  // Get documents by task
  getDocumentsByTask(taskId: number): Observable<Document[]> {
    return this.http.get<Document[]>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/task/${taskId}`
    );
  }

  // Get all documents (admin view)
  getAllDocuments(): Observable<Document[]> {
    return this.http.get<Document[]>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/all`
    );
  }

  // Get documents with filters
  getDocumentsWithFilters(filters: DocumentFilters = {}): Observable<any> {
    let url = `${API_CONFIG.baseUrl}${API_CONFIG.documents}/filter`;
    const params = new URLSearchParams();

    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.append(key, value.toString());
      }
    });

    if (params.toString()) {
      url += `?${params.toString()}`;
    }

    return this.http.get<any>(url);
  }

  // Update document verification status
  updateDocumentVerification(
    documentId: number,
    isVerified: boolean,
    verifiedBy?: string
  ): Observable<Document> {
    let params = new HttpParams().set('isVerified', isVerified.toString());

    // Only add verifiedBy parameter if it's provided and not empty
    if (verifiedBy && verifiedBy.trim() !== '') {
      params = params.set('verifiedBy', verifiedBy);
    }

    return this.http.put<Document>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/${documentId}/verify`,
      null,
      { params }
    );
  }

  rejectDocument(
    documentId: number,
    rejectionReason: string
  ): Observable<Document> {
    const params = new HttpParams().set('rejectionReason', rejectionReason);

    return this.http.put<Document>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/${documentId}/reject`,
      null,
      { params }
    );
  }

  // Delete document
  deleteDocument(id: number): Observable<void> {
    return this.http.delete<void>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/${id}`
    );
  }

  // Download document
  downloadDocument(id: number): Observable<Blob> {
    return this.http.get(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/${id}/download`,
      { responseType: 'blob' }
    );
  }

  // Get document statistics
  getDocumentStatistics(): Observable<DocumentStatistics> {
    return this.http.get<DocumentStatistics>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/statistics`
    );
  }

  // Get documents by type
  getDocumentsByType(documentType: string): Observable<Document[]> {
    return this.http.get<Document[]>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/type/${documentType}`
    );
  }

  // Get documents by verification status
  getDocumentsByVerificationStatus(
    isVerified: boolean
  ): Observable<Document[]> {
    return this.http.get<Document[]>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/verification/${isVerified}`
    );
  }

  // Search documents
  searchDocuments(searchTerm: string): Observable<Document[]> {
    const params = new HttpParams().set('term', searchTerm);
    return this.http.get<Document[]>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/search`,
      { params }
    );
  }

  // Get document count by client
  getDocumentCountByClient(clientId: number): Observable<number> {
    return this.http.get<number>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/client/${clientId}/count`
    );
  }

  // Get verified document count by client
  getVerifiedDocumentCountByClient(clientId: number): Observable<number> {
    return this.http.get<number>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/client/${clientId}/verified-count`
    );
  }

  // ========== CLIENT-SPECIFIC METHODS ==========

  // Get my documents (client view)
  getMyDocuments(
    page: number = 0,
    size: number = 10,
    documentType?: string,
    status?: string
  ): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (documentType) {
      params = params.set('documentType', documentType);
    }

    if (status) {
      params = params.set('status', status);
    }

    return this.http.get<any>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/my-documents`,
      { params }
    );
  }

  // Get my document statistics
  getMyDocumentStatistics(): Observable<DocumentStatistics> {
    return this.http.get<DocumentStatistics>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/my-documents/statistics`
    );
  }

  // Upload my document
  uploadMyDocument(
    file: File,
    documentType: string,
    taskId?: number
  ): Observable<Document> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('documentType', documentType);
    if (taskId) {
      formData.append('taskId', taskId.toString());
    }

    return this.http.post<Document>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/my-documents/upload`,
      formData
    );
  }

  // Get my document by ID
  getMyDocumentById(id: number): Observable<Document> {
    return this.http.get<Document>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/my-documents/${id}`
    );
  }

  // Download my document
  downloadMyDocument(id: number): Observable<Blob> {
    return this.http.get(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/my-documents/${id}/download`,
      { responseType: 'blob' }
    );
  }

  // Search my documents
  searchMyDocuments(searchTerm: string): Observable<Document[]> {
    const params = new HttpParams().set('term', searchTerm);
    return this.http.get<Document[]>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/my-documents/search`,
      { params }
    );
  }

  // Get my documents by type
  getMyDocumentsByType(documentType: string): Observable<Document[]> {
    return this.http.get<Document[]>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/my-documents/type/${documentType}`
    );
  }

  // ========== STAFF-SPECIFIC METHODS ==========

  // Get documents for tasks assigned to current staff member
  getStaffDocuments(
    page: number = 0,
    size: number = 10,
    searchTerm?: string,
    status?: string,
    documentType?: string,
    clientId?: number,
    taskId?: number
  ): Observable<{
    content: Document[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
  }> {
    let params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    if (searchTerm) params.append('searchTerm', searchTerm);
    if (status) params.append('status', status);
    if (documentType) params.append('documentType', documentType);
    if (clientId) params.append('clientId', clientId.toString());
    if (taskId) params.append('taskId', taskId.toString());

    return this.http.get<{
      content: Document[];
      totalElements: number;
      totalPages: number;
      size: number;
      number: number;
    }>(
      `${API_CONFIG.baseUrl}${
        API_CONFIG.documents
      }/staff-documents?${params.toString()}`
    );
  }

  // Get staff document statistics
  getStaffDocumentStatistics(): Observable<DocumentStatistics> {
    return this.http.get<DocumentStatistics>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/staff-documents/statistics`
    );
  }

  // Upload document for staff (for assigned clients)
  uploadStaffDocument(
    file: File,
    clientId: number,
    documentType: string,
    taskId?: number
  ): Observable<Document> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('clientId', clientId.toString());
    formData.append('documentType', documentType);
    if (taskId) {
      formData.append('taskId', taskId.toString());
    }

    return this.http.post<Document>(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/staff-documents/upload`,
      formData
    );
  }

  // Verify document (staff can verify documents)
  verifyStaffDocument(
    documentId: number,
    isVerified: boolean,
    verifiedBy?: string
  ): Observable<Document> {
    let params = new URLSearchParams();
    params.append('isVerified', isVerified.toString());
    // Only add verifiedBy parameter if it's provided and not empty
    if (verifiedBy && verifiedBy.trim() !== '') {
      params.append('verifiedBy', verifiedBy);
    }

    return this.http.put<Document>(
      `${API_CONFIG.baseUrl}${
        API_CONFIG.documents
      }/staff-documents/${documentId}/verify?${params.toString()}`,
      {}
    );
  }

  // Reject document (staff can reject documents)
  rejectStaffDocument(
    documentId: number,
    rejectionReason: string
  ): Observable<Document> {
    const params = new URLSearchParams();
    params.append('rejectionReason', rejectionReason);

    return this.http.put<Document>(
      `${API_CONFIG.baseUrl}${
        API_CONFIG.documents
      }/staff-documents/${documentId}/reject?${params.toString()}`,
      {}
    );
  }

  // Download staff document
  downloadStaffDocument(id: number): Observable<Blob> {
    return this.http.get(
      `${API_CONFIG.baseUrl}${API_CONFIG.documents}/staff-documents/${id}/download`,
      { responseType: 'blob' }
    );
  }

  // Get document type display name
  getDocumentTypeDisplayName(documentType: DocumentType): string {
    const displayNames: { [key in DocumentType]: string } = {
      [DocumentType.PAN_CARD]: 'PAN Card',
      [DocumentType.AADHAR]: 'Aadhaar Card',
      [DocumentType.BANK_STATEMENT]: 'Bank Statement',
      [DocumentType.INVOICE]: 'Invoice',
      [DocumentType.FORM_16]: 'Form 16',
      [DocumentType.GST_RETURN]: 'GST Return',
      [DocumentType.COMPANY_DOCS]: 'Company Documents',
      [DocumentType.OTHER]: 'Other',
    };
    return displayNames[documentType] || documentType;
  }

  // Format file size
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  // Get file icon based on file type
  getFileIcon(fileType: string): string {
    if (fileType.includes('pdf')) return 'picture_as_pdf';
    if (fileType.includes('image')) return 'image';
    if (fileType.includes('word') || fileType.includes('document'))
      return 'description';
    if (fileType.includes('excel') || fileType.includes('spreadsheet'))
      return 'table_chart';
    return 'insert_drive_file';
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../api.config';

export interface ServiceCategory {
  id: number;
  name: string;
  description: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  subcategories?: ServiceSubcategory[];
}

export interface ServiceSubcategory {
  id: number;
  name: string;
  description: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  categoryId: number;
  items?: ServiceItem[];
}

export interface ServiceItem {
  id: number;
  name: string;
  description: string;
  estimatedHours: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  subcategoryId: number;
  subcategoryName: string;
  categoryId: number;
  categoryName: string;
}

@Injectable({
  providedIn: 'root',
})
export class ServiceHierarchyService {
  private baseUrl = API_CONFIG.baseUrl;

  constructor(private http: HttpClient) {}

  // Get full service hierarchy (categories with subcategories and items)
  getFullHierarchy(): Observable<ServiceCategory[]> {
    return this.http.get<ServiceCategory[]>(
      `${this.baseUrl}/api/services/hierarchy`
    );
  }

  // Get all categories
  getAllCategories(): Observable<ServiceCategory[]> {
    return this.http.get<ServiceCategory[]>(
      `${this.baseUrl}/api/services/categories`
    );
  }

  // Get subcategories by category ID
  getSubcategoriesByCategory(
    categoryId: number
  ): Observable<ServiceSubcategory[]> {
    return this.http.get<ServiceSubcategory[]>(
      `${this.baseUrl}/api/services/categories/${categoryId}/subcategories`
    );
  }

  // Get service items by subcategory ID
  getServiceItemsBySubcategory(
    subcategoryId: number
  ): Observable<ServiceItem[]> {
    return this.http.get<ServiceItem[]>(
      `${this.baseUrl}/api/services/subcategories/${subcategoryId}/items`
    );
  }

  // Get all service items
  getAllServiceItems(): Observable<ServiceItem[]> {
    return this.http.get<ServiceItem[]>(`${this.baseUrl}/api/services/items`);
  }

  // Get specific service item by ID
  getServiceItemById(itemId: number): Observable<ServiceItem> {
    return this.http.get<ServiceItem>(
      `${this.baseUrl}/api/services/items/${itemId}`
    );
  }

  // Admin operations (require authentication)
  createCategory(
    category: Partial<ServiceCategory>
  ): Observable<ServiceCategory> {
    return this.http.post<ServiceCategory>(
      `${this.baseUrl}/api/services/categories`,
      category
    );
  }

  createSubcategory(
    subcategory: Partial<ServiceSubcategory>
  ): Observable<ServiceSubcategory> {
    return this.http.post<ServiceSubcategory>(
      `${this.baseUrl}/api/services/subcategories`,
      subcategory
    );
  }

  createServiceItem(item: Partial<ServiceItem>): Observable<ServiceItem> {
    return this.http.post<ServiceItem>(
      `${this.baseUrl}/api/services/items`,
      item
    );
  }

  updateCategory(
    id: number,
    category: Partial<ServiceCategory>
  ): Observable<ServiceCategory> {
    return this.http.put<ServiceCategory>(
      `${this.baseUrl}/api/services/categories/${id}`,
      category
    );
  }

  updateSubcategory(
    id: number,
    subcategory: Partial<ServiceSubcategory>
  ): Observable<ServiceSubcategory> {
    return this.http.put<ServiceSubcategory>(
      `${this.baseUrl}/api/services/subcategories/${id}`,
      subcategory
    );
  }

  updateServiceItem(
    id: number,
    item: Partial<ServiceItem>
  ): Observable<ServiceItem> {
    return this.http.put<ServiceItem>(
      `${this.baseUrl}/api/services/items/${id}`,
      item
    );
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/api/services/categories/${id}`
    );
  }

  deleteSubcategory(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/api/services/subcategories/${id}`
    );
  }

  deleteServiceItem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/services/items/${id}`);
  }
}

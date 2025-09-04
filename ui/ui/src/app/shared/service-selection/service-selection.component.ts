import {
  Component,
  OnInit,
  Input,
  Output,
  EventEmitter,
  forwardRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import {
  ServiceHierarchyService,
  ServiceCategory,
  ServiceSubcategory,
  ServiceItem,
} from '../../services/service-hierarchy.service';

@Component({
  selector: 'app-service-selection',
  templateUrl: './service-selection.component.html',
  styleUrls: ['./service-selection.component.scss'],
  standalone: true,
  imports: [CommonModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ServiceSelectionComponent),
      multi: true,
    },
  ],
})
export class ServiceSelectionComponent implements OnInit, ControlValueAccessor {
  @Input() placeholder: string = 'Select a service';
  @Input() showEstimatedHours: boolean = true;
  @Input() showServiceDescription: boolean = true;
  @Input() required: boolean = false;
  @Input() disabled: boolean = false;
  @Output() serviceSelected = new EventEmitter<ServiceItem>();

  categories: ServiceCategory[] = [];
  subcategories: ServiceSubcategory[] = [];
  serviceItems: ServiceItem[] = [];

  selectedCategoryId: number | null = null;
  selectedSubcategoryId: number | null = null;
  selectedServiceItemId: number | null = null;

  selectedServiceItem: ServiceItem | null = null;

  loading = false;
  error: string | null = null;

  // ControlValueAccessor implementation
  private onChange = (value: any) => {};
  private onTouched = () => {};

  constructor(private serviceHierarchyService: ServiceHierarchyService) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.error = null;

    this.serviceHierarchyService.getAllCategories().subscribe({
      next: (categories) => {
        this.categories = categories.filter((cat) => cat.active);

        // If we have a pre-selected category, load its subcategories
        if (this.selectedCategoryId) {
          this.loadSubcategories(this.selectedCategoryId);
        } else {
          this.loading = false;
        }
      },
      error: (error) => {
        console.error('Error loading categories:', error);
        this.error = 'Failed to load service categories';
        this.loading = false;
      },
    });
  }

  onCategoryChange(categoryId: number): void {
    this.selectedCategoryId = categoryId;
    this.selectedSubcategoryId = null;
    this.selectedServiceItemId = null;
    this.selectedServiceItem = null;
    this.subcategories = [];
    this.serviceItems = [];

    if (categoryId) {
      this.loadSubcategories(categoryId);
    }

    this.onChange(this.selectedServiceItem);
    this.onTouched();
  }

  loadSubcategories(categoryId: number): void {
    this.loading = true;
    this.error = null;

    this.serviceHierarchyService
      .getSubcategoriesByCategory(categoryId)
      .subscribe({
        next: (subcategories) => {
          this.subcategories = subcategories.filter((sub) => sub.active);

          // If we have a pre-selected subcategory, load its service items
          if (this.selectedSubcategoryId) {
            this.loadServiceItems(this.selectedSubcategoryId);
          } else {
            this.loading = false;
          }
        },
        error: (error) => {
          console.error('Error loading subcategories:', error);
          this.error = 'Failed to load subcategories';
          this.loading = false;
        },
      });
  }

  onSubcategoryChange(subcategoryId: number): void {
    this.selectedSubcategoryId = subcategoryId;
    this.selectedServiceItemId = null;
    this.selectedServiceItem = null;
    this.serviceItems = [];

    if (subcategoryId) {
      this.loadServiceItems(subcategoryId);
    }

    this.onChange(this.selectedServiceItem);
    this.onTouched();
  }

  loadServiceItems(subcategoryId: number): void {
    this.loading = true;
    this.error = null;

    this.serviceHierarchyService
      .getServiceItemsBySubcategory(subcategoryId)
      .subscribe({
        next: (items) => {
          this.serviceItems = items.filter((item) => item.active);
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading service items:', error);
          this.error = 'Failed to load service items';
          this.loading = false;
        },
      });
  }

  loadServiceItemById(serviceItemId: number): void {
    this.loading = true;
    this.error = null;

    this.serviceHierarchyService.getServiceItemById(serviceItemId).subscribe({
      next: (serviceItem) => {
        this.selectedServiceItem = serviceItem;
        this.selectedServiceItemId = serviceItem.id;
        this.selectedSubcategoryId = serviceItem.subcategoryId;
        this.selectedCategoryId = serviceItem.categoryId;

        // Load the hierarchy to populate the dropdowns
        this.loadCategories();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading service item:', error);
        this.error = 'Failed to load service item';
        this.loading = false;
      },
    });
  }

  onServiceItemChange(itemId: number): void {
    this.selectedServiceItemId = itemId;

    if (itemId) {
      this.selectedServiceItem =
        this.serviceItems.find((item) => item.id === itemId) || null;
      this.serviceSelected.emit(this.selectedServiceItem!);
    } else {
      this.selectedServiceItem = null;
    }

    this.onChange(itemId); // Pass just the ID, not the entire object
    this.onTouched();
  }

  onCategoryChangeEvent(event: Event): void {
    const target = event.target as HTMLSelectElement;
    if (target) {
      this.onCategoryChange(+target.value);
    }
  }

  onSubcategoryChangeEvent(event: Event): void {
    const target = event.target as HTMLSelectElement;
    if (target) {
      this.onSubcategoryChange(+target.value);
    }
  }

  onServiceItemChangeEvent(event: Event): void {
    const target = event.target as HTMLSelectElement;
    if (target) {
      this.onServiceItemChange(+target.value);
    }
  }

  getSelectedCategoryName(): string {
    if (!this.selectedCategoryId) return '';
    const category = this.categories.find(
      (cat) => cat.id === this.selectedCategoryId
    );
    return category ? category.name : '';
  }

  getSelectedSubcategoryName(): string {
    if (!this.selectedSubcategoryId) return '';
    const subcategory = this.subcategories.find(
      (sub) => sub.id === this.selectedSubcategoryId
    );
    return subcategory ? subcategory.name : '';
  }

  getSelectedServiceItemName(): string {
    if (!this.selectedServiceItemId) return '';
    const item = this.serviceItems.find(
      (item) => item.id === this.selectedServiceItemId
    );
    return item ? item.name : '';
  }

  // ControlValueAccessor methods
  writeValue(value: ServiceItem | null | number): void {
    if (value) {
      if (typeof value === 'number') {
        // If we only have an ID, load the service item
        this.loadServiceItemById(value);
      } else {
        // If we have a complete ServiceItem object
        this.selectedServiceItem = value;
        this.selectedServiceItemId = value.id;
        this.selectedSubcategoryId = value.subcategoryId;
        this.selectedCategoryId = value.categoryId;

        // Load the hierarchy if not already loaded
        if (this.categories.length === 0) {
          this.loadCategories();
        }
      }
    } else {
      // Clear the selection when value is null/undefined
      this.selectedServiceItem = null;
      this.selectedServiceItemId = null;
      this.selectedSubcategoryId = null;
      this.selectedCategoryId = null;
      this.subcategories = [];
      this.serviceItems = [];
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    // Implement if needed
  }

  clearSelection(): void {
    this.selectedCategoryId = null;
    this.selectedSubcategoryId = null;
    this.selectedServiceItemId = null;
    this.selectedServiceItem = null;
    this.subcategories = [];
    this.serviceItems = [];

    this.onChange(null);
    this.onTouched();
  }
}

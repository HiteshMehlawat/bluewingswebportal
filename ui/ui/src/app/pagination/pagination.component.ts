import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pagination.component.html',
})
export class PaginationComponent implements OnInit, OnChanges {
  @Input() page = 1;
  @Input() pageSize = 5;
  @Input() total = 0;
  @Input() pageSizeOptions: number[] = [5, 10, 20];
  @Output() pageChange = new EventEmitter<number>();
  @Output() pageSizeChange = new EventEmitter<number>();

  // Use a getter to always return the current pageSize
  get currentPageSize(): number {
    return this.pageSize;
  }

  ngOnInit() {
    // No need to initialize currentPageSize since we're using a getter
  }

  ngOnChanges(changes: SimpleChanges) {
    // No need to do anything here since currentPageSize is a getter
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.total / this.pageSize));
  }

  getSummary(): string {
    if (this.total === 0) return 'No records';
    const start = (this.page - 1) * this.pageSize + 1;
    const end = Math.min(start + this.pageSize - 1, this.total);
    return `Showing ${start}-${end} of ${this.total}`;
  }

  goToPage(page: number) {
    if (page < 1 || page > this.totalPages) return;
    this.pageChange.emit(page);
  }

  onPageSizeChange(size: string | number) {
    const newSize = Number(size);
    this.pageSizeChange.emit(newSize);
    // Reset to page 1 when page size changes
    if (this.page !== 1) {
      this.pageChange.emit(1);
    }
  }

  // Force update the currentPageSize
  updateCurrentPageSize(size: number) {
    // No need to update currentPageSize here, it's a getter
  }

  onPageSizeChangeEvent(event: Event) {
    const value = (event.target as HTMLSelectElement).value;
    this.onPageSizeChange(value);
  }

  onPageInputChange(event: Event) {
    const value = (event.target as HTMLInputElement).valueAsNumber;
    this.goToPage(value);
  }
}

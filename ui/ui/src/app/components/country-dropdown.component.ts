import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  forwardRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormsModule,
  ControlValueAccessor,
  NG_VALUE_ACCESSOR,
} from '@angular/forms';
import { CountryService, Country } from '../services/country.service';

@Component({
  selector: 'app-country-dropdown',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="relative">
      <input
        type="text"
        [(ngModel)]="selectedCountryName"
        (input)="onCountryInput($event)"
        (focus)="onCountryFocus()"
        (blur)="onCountryBlur()"
        (keydown)="onCountryKeydown($event)"
        [placeholder]="placeholder"
        class="block w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white"
        [class.border-red-500]="showError"
        [disabled]="disabled"
      />

      <!-- Suggestions Dropdown -->
      <div
        *ngIf="showCountrySuggestions && filteredCountries.length > 0"
        class="absolute z-50 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto"
      >
        <div
          *ngFor="let country of filteredCountries; let i = index"
          (mousedown)="
            selectCountry(country);
            $event.preventDefault();
            $event.stopPropagation()
          "
          (mouseover)="selectedSuggestionIndex = i"
          (mouseout)="selectedSuggestionIndex = -1"
          class="cursor-pointer hover:bg-blue-50 py-2 px-3 border-b border-gray-100 last:border-b-0 transition-colors duration-150"
          [class.bg-blue-100]="i === selectedSuggestionIndex"
        >
          <div class="flex justify-between items-center">
            <div class="flex items-center">
              <span class="font-medium text-blue-600">{{ country.name }}</span>
              <span class="text-xs text-gray-400 ml-1">(will be selected)</span>
            </div>
            <span class="text-sm text-gray-500">{{ country.phoneCode }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Error Message -->
    <div *ngIf="showError && errorMessage" class="mt-1 text-sm text-red-600">
      {{ errorMessage }}
    </div>
  `,
  styles: [
    `
      select {
        max-height: 200px;
      }
      select option {
        padding: 8px 12px;
      }
    `,
  ],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CountryDropdownComponent),
      multi: true,
    },
  ],
})
export class CountryDropdownComponent implements OnInit, ControlValueAccessor {
  @Input() placeholder: string = 'Select a country';
  @Input() disabled: boolean = false;
  @Input() required: boolean = false;
  @Input() errorMessage: string = '';
  @Input() showError: boolean = false;

  @Output() countryChange = new EventEmitter<string>();

  countries: Country[] = [];
  selectedCountry: string = '';
  selectedCountryName: string = '';

  // Suggestion related properties
  filteredCountries: Country[] = [];
  showCountrySuggestions = false;
  selectedSuggestionIndex = -1;

  private onChange = (value: string) => {};
  private onTouched = () => {};

  constructor(private countryService: CountryService) {}

  ngOnInit() {
    this.countries = this.countryService.getAllCountries();
  }

  onCountryInput(event: any) {
    const value = event.target.value;
    this.selectedCountryName = value;
    this.filterCountries(value);
  }

  onCountryFocus() {
    this.filterCountries(this.selectedCountryName);
    this.showCountrySuggestions = true;
  }

  onCountryBlur() {
    // Delay hiding suggestions to allow for clicks
    setTimeout(() => {
      this.showCountrySuggestions = false;
      this.selectedSuggestionIndex = -1;
    }, 150);
  }

  onCountryKeydown(event: KeyboardEvent) {
    if (!this.showCountrySuggestions) return;

    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        this.selectedSuggestionIndex = Math.min(
          this.selectedSuggestionIndex + 1,
          this.filteredCountries.length - 1
        );
        break;
      case 'ArrowUp':
        event.preventDefault();
        this.selectedSuggestionIndex = Math.max(
          this.selectedSuggestionIndex - 1,
          -1
        );
        break;
      case 'Enter':
        event.preventDefault();
        if (
          this.selectedSuggestionIndex >= 0 &&
          this.selectedSuggestionIndex < this.filteredCountries.length
        ) {
          this.selectCountry(
            this.filteredCountries[this.selectedSuggestionIndex]
          );
        }
        break;
      case 'Escape':
        this.showCountrySuggestions = false;
        this.selectedSuggestionIndex = -1;
        break;
    }
  }

  filterCountries(searchTerm: string) {
    if (!searchTerm) {
      this.filteredCountries = this.countries.slice(0, 10); // Show first 10 countries
    } else {
      this.filteredCountries = this.countries
        .filter(
          (country) =>
            country.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            country.code.toLowerCase().includes(searchTerm.toLowerCase())
        )
        .slice(0, 10); // Limit to 10 results
    }
  }

  selectCountry(country: Country) {
    this.selectedCountry = country.code;
    this.selectedCountryName = country.name;
    this.showCountrySuggestions = false;
    this.selectedSuggestionIndex = -1;
    this.onChange(this.selectedCountryName); // Store country name instead of code
    this.countryChange.emit(this.selectedCountryName); // Emit country name
    this.onTouched(); // Mark as touched for form validation
  }

  // ControlValueAccessor implementation
  writeValue(value: string): void {
    this.selectedCountryName = value || '';
    if (value) {
      // Try to find country by name first, then by code
      const country =
        this.countryService.getCountryByName(value) ||
        this.countryService.getCountryByCode(value);
      if (country) {
        this.selectedCountry = country.code;
        this.selectedCountryName = country.name;
      } else {
        // If not found, assume it's a country name
        this.selectedCountry = '';
        this.selectedCountryName = value;
      }
    } else {
      this.selectedCountry = '';
      this.selectedCountryName = '';
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  // Get the selected country object
  getSelectedCountry(): Country | undefined {
    return this.countryService.getCountryByCode(this.selectedCountry);
  }

  // Get the selected country name
  getSelectedCountryName(): string {
    const country = this.getSelectedCountry();
    return country ? country.name : '';
  }
}

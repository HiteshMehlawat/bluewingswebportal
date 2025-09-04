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
  selector: 'app-phone-input',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="flex">
      <!-- Country Code Input with Suggestions -->
      <div class="relative">
        <input
          type="text"
          [(ngModel)]="selectedCountryCode"
          (input)="onCountryCodeInput($event)"
          (focus)="onCountryCodeFocus()"
          (blur)="onCountryCodeBlur()"
          (keydown)="onCountryCodeKeydown($event)"
          placeholder="+91"
          class="block w-28 px-3 py-2 text-sm border border-gray-300 rounded-l-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white"
          [class.border-red-500]="showError"
          [disabled]="disabled"
        />

        <!-- Suggestions Dropdown -->
        <div
          *ngIf="showCountrySuggestions && filteredCountries.length > 0"
          class="absolute z-50 w-64 mt-1 bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto"
        >
          <div
            *ngFor="let country of filteredCountries; let i = index"
            (mousedown)="
              selectCountryCode(country);
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
                <span class="font-medium text-blue-600">{{
                  country.phoneCode
                }}</span>
                <span class="text-xs text-gray-400 ml-1"
                  >(will be selected)</span
                >
              </div>
              <span class="text-sm text-gray-500">{{ country.name }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Phone Number Input -->
      <input
        type="tel"
        [(ngModel)]="phoneNumber"
        (input)="onPhoneNumberChange()"
        [placeholder]="placeholder"
        class="flex-1 block w-full px-3 py-2 text-sm border border-l-0 border-gray-300 rounded-r-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
        [class.border-red-500]="showError"
        [disabled]="disabled"
      />
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
      useExisting: forwardRef(() => PhoneInputComponent),
      multi: true,
    },
  ],
})
export class PhoneInputComponent implements OnInit, ControlValueAccessor {
  @Input() placeholder: string = 'Enter phone number';
  @Input() disabled: boolean = false;
  @Input() required: boolean = false;
  @Input() errorMessage: string = '';
  @Input() showError: boolean = false;

  @Output() phoneChange = new EventEmitter<string>();

  countries: Country[] = [];
  selectedCountryCode: string = '+91'; // Default to India
  phoneNumber: string = '';

  // Suggestion related properties
  filteredCountries: Country[] = [];
  showCountrySuggestions = false;
  selectedSuggestionIndex = -1;

  private onChange = (value: string) => {};
  private onTouched = () => {};

  constructor(private countryService: CountryService) {}

  ngOnInit() {
    this.countries = this.countryService.getAllCountries();
    // Set default to India if no value is set
    if (!this.phoneNumber) {
      this.selectedCountryCode = '+91';
    }
  }

  onCountryCodeInput(event: any) {
    const value = event.target.value;
    this.selectedCountryCode = value;
    this.filterCountries(value);
    this.updateValue();
  }

  onCountryCodeFocus() {
    this.filterCountries(this.selectedCountryCode);
    this.showCountrySuggestions = true;
  }

  onCountryCodeBlur() {
    // Delay hiding suggestions to allow for clicks
    setTimeout(() => {
      this.showCountrySuggestions = false;
      this.selectedSuggestionIndex = -1;
    }, 150);
  }

  onCountryCodeKeydown(event: KeyboardEvent) {
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
          this.selectCountryCode(
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
            country.phoneCode.includes(searchTerm) ||
            country.name.toLowerCase().includes(searchTerm.toLowerCase())
        )
        .slice(0, 10); // Limit to 10 results
    }
  }

  selectCountryCode(country: Country) {
    this.selectedCountryCode = country.phoneCode;
    this.showCountrySuggestions = false;
    this.selectedSuggestionIndex = -1;
    this.updateValue();
    this.onTouched(); // Mark as touched for form validation
  }

  onPhoneNumberChange() {
    this.updateValue();
  }

  private updateValue() {
    const fullPhoneNumber = this.countryService.combinePhoneNumber(
      this.selectedCountryCode,
      this.phoneNumber
    );
    this.onChange(fullPhoneNumber);
    this.phoneChange.emit(fullPhoneNumber);
  }

  // ControlValueAccessor implementation
  writeValue(value: string): void {
    if (value) {
      const parsed = this.countryService.parsePhoneNumber(value);
      if (parsed) {
        this.selectedCountryCode = parsed.countryCode;
        this.phoneNumber = parsed.number;
      } else {
        // If we can't parse, assume it's just a number without country code
        this.phoneNumber = value;
      }
    } else {
      this.phoneNumber = '';
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

  // Get the full phone number
  getFullPhoneNumber(): string {
    return this.countryService.combinePhoneNumber(
      this.selectedCountryCode,
      this.phoneNumber
    );
  }

  // Get just the country code
  getCountryCode(): string {
    return this.selectedCountryCode;
  }

  // Get just the phone number
  getPhoneNumber(): string {
    return this.phoneNumber;
  }
}

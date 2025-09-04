# Reusable Components

This directory contains reusable components for the Tax Consultancy Web Portal.

## Phone Input Component (`phone-input.component.ts`)

A reusable phone input component with searchable country code input that automatically combines the country code with the phone number.

### Features:

- **Searchable country code input** - Type to search for country codes
- **Smart suggestions** - Shows matching country codes and country names
- **Keyboard navigation** - Use arrow keys, Enter, and Escape
- **Automatic phone number formatting** - Combines country code with phone number
- **Form control integration** - Works with Reactive Forms
- **Error handling** - Visual error indicators
- **Responsive design** - Works on all screen sizes

### Usage:

```typescript
// In your component
import { PhoneInputComponent } from '../components/phone-input.component';

@Component({
  imports: [PhoneInputComponent],
  // ...
})
```

```html
<!-- In your template -->
<app-phone-input formControlName="phone" placeholder="Enter phone number" [showError]="form.get('phone')?.invalid && form.get('phone')?.touched" errorMessage="Phone number is required"> </app-phone-input>
```

### Input Properties:

- `placeholder`: Placeholder text for the phone input
- `disabled`: Whether the input is disabled
- `required`: Whether the field is required
- `errorMessage`: Error message to display
- `showError`: Whether to show error styling

### Output Events:

- `phoneChange`: Emitted when the phone number changes

## Country Dropdown Component (`country-dropdown.component.ts`)

A reusable searchable country input component for selecting countries.

### Features:

- **Searchable country input** - Type to search for countries
- **Smart suggestions** - Shows matching country names and phone codes
- **Keyboard navigation** - Use arrow keys, Enter, and Escape
- **Complete list of countries** with ISO codes and phone codes
- **Form control integration** - Works with Reactive Forms
- **Error handling** - Visual error indicators
- **Responsive design** - Works on all screen sizes

### Usage:

```typescript
// In your component
import { CountryDropdownComponent } from '../components/country-dropdown.component';

@Component({
  imports: [CountryDropdownComponent],
  // ...
})
```

```html
<!-- In your template -->
<app-country-dropdown formControlName="country" placeholder="Select a country" [showError]="form.get('country')?.invalid && form.get('country')?.touched" errorMessage="Country is required"> </app-country-dropdown>
```

### Input Properties:

- `placeholder`: Placeholder text for the dropdown
- `disabled`: Whether the dropdown is disabled
- `required`: Whether the field is required
- `errorMessage`: Error message to display
- `showError`: Whether to show error styling

### Output Events:

- `countryChange`: Emitted when the country selection changes

## Country Service (`country.service.ts`)

A service that provides country data and utility functions.

### Methods:

- `getAllCountries()`: Returns all countries sorted alphabetically
- `getCountryByCode(code)`: Returns a country by its ISO code
- `getCountryByPhoneCode(phoneCode)`: Returns a country by its phone code
- `parsePhoneNumber(phoneNumber)`: Parses a full phone number to extract country code and number
- `combinePhoneNumber(countryCode, phoneNumber)`: Combines country code and phone number

### Usage:

```typescript
// In your component
import { CountryService } from '../services/country.service';

constructor(private countryService: CountryService) {}

// Get all countries
const countries = this.countryService.getAllCountries();

// Parse a phone number
const parsed = this.countryService.parsePhoneNumber('+91-1234567890');
// Returns: { countryCode: '+91', number: '1234567890' }

// Combine country code and phone number
const fullNumber = this.countryService.combinePhoneNumber('+91', '1234567890');
// Returns: '+91-1234567890'
```

## Implementation Notes

### Phone Number Format:

- The phone input component automatically combines the selected country code with the entered phone number
- When a form is submitted, the phone field will contain the full international format (e.g., "+91-1234567890")
- The component can parse existing phone numbers to populate the country code dropdown correctly

### Country Codes:

- Countries are stored with their full names (e.g., "India" instead of "IN")
- Phone codes are stored with the "+" prefix (e.g., "+91" for India)
- The country dropdown displays and stores country names for better readability

### Form Integration:

- Both components implement `ControlValueAccessor` for seamless form integration
- They work with both Template-driven and Reactive forms
- Error states are handled through the `showError` and `errorMessage` inputs

### Styling:

- Components use Tailwind CSS classes for consistent styling
- Error states are visually indicated with red borders and error messages
- The design is responsive and matches the overall application theme

import { Directive, ElementRef, forwardRef, HostListener, Renderer2 } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Directive({
  selector: '[contenteditable][formControlName],[contenteditable][formControl],[contenteditable][ngModel]',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ContenteditableValueAccessor),
      multi: true,
    },
  ],
})
export class ContenteditableValueAccessor implements ControlValueAccessor {
  constructor(private elementRef: ElementRef, private renderer: Renderer2) {}

  @HostListener('input', ['$event.target.innerHTML'])
  onInput(value: string): void {
    this.onChange(value);
  }

  @HostListener('blur')
  onTouched(): void {
    this.onTouchedCallback();
  }

  writeValue(value: any): void {
    this.renderer.setProperty(this.elementRef.nativeElement, 'innerHTML', value);
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouchedCallback = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    this.renderer.setProperty(this.elementRef.nativeElement, 'disabled', isDisabled);
  }

  private onChange: (value: any) => void = () => {};
  private onTouchedCallback: () => void = () => {};
}
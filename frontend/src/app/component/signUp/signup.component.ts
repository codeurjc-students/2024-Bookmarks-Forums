import { Component, OnInit } from '@angular/core';
import { UserService } from '../../services/user.service';
import { Chart, registerables } from 'chart.js';
import { Router } from '@angular/router';
import { FormControl } from '@angular/forms';
import { debounceTime, switchMap } from 'rxjs';
import { TitleService } from '../../services/title.service';
Chart.register(...registerables);

@Component({
  selector: 'app-login',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css', '../../../animations.css'],
})
export class SignupComponent implements OnInit {
  title = 'Bookmarks';

  usernameNotAvailable = false;
  passwordsDontMatch = false;
  passwordMissingRequirements = false;
  confirmPasswordTouched = false;
  emailNotValid = false;

  usernameControl = new FormControl();
  passwordControl = new FormControl('');
  confirmPasswordControl = new FormControl('');

  constructor(public userService: UserService, private router: Router, private titleService: TitleService) {}

  ngOnInit() {
    this.titleService.setTitle('Crear cuenta');
    this.usernameControl.valueChanges
      .pipe(
        // Debounce time to avoid too many requests
        debounceTime(300),
        switchMap((userName) => this.userService.isUsernameTaken(userName))
      )
      .subscribe({
        next: (r) => {
          this.usernameNotAvailable = r;
        },
      });

    this.passwordControl.valueChanges.subscribe((value) => {
      this.passwordsDontMatch = value !== this.confirmPasswordControl.value;
      this.passwordMissingRequirements = !this.checkPasswordRequirements(
        value as string
      );
    });

    this.confirmPasswordControl.valueChanges.subscribe((value) => {
      this.confirmPasswordTouched = true;
      this.passwordsDontMatch = value !== this.passwordControl.value;
    });
  }

  checkPasswordRequirements(password: string): boolean {
    // Password requirements
    // At least 8 characters
    // At least 1 digit
    // At least 1 lowercase letter
    // At least 1 uppercase letter
    // At least 1 special character
    return /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$/.test(password);
  }

  signUp(
    userName: string,
    userAlias: string,
    userEmail: string,
    userPassword: string
  ) {
    if (
      !this.usernameNotAvailable &&
      !this.passwordsDontMatch &&
      !this.passwordMissingRequirements
    ) {
      this.userService
        .signup({
          username: userName,
          email: userEmail,
          alias: userAlias,
          password: userPassword,
        })
        .subscribe({
          next: (r) => {
            this.router.navigate(['/login']);
          },
          error: (r) => {
            // if the error has code 409, invalid email address provided
            if (r.status === 409) {
              this.emailNotValid = true;
              return;
            }
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error al registrarse',
                description:
                  'Se ha producido un error al intentar registrarse. Por favor, inténtelo de nuevo más tarde.',
              },
            });
          },
        });
    }
  }
}

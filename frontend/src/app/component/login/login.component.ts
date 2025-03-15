import {Component, OnInit} from "@angular/core";
import {LoginService} from "../../services/session.service";
import {Chart, registerables} from "chart.js";
import {Router} from "@angular/router";
import { TitleService } from '../../services/title.service';
Chart.register(...registerables);

@Component({
  selector: "app-login",
  templateUrl: "./login.component.html",
  styleUrls: ["./login.component.css", "../../../animations.css"],
})

export class LoginComponent implements OnInit {
  title = "Bookmarks";
  errorMessage: string | null = null;

  constructor(public loginService:LoginService, private router:Router, private titleService: TitleService) {
  }

  ngOnInit() {
    this.titleService.setTitle('Iniciar sesión');
  }

  logIn(userName: string, userPassword: string) {
    this.loginService.login({ username: userName, password: userPassword }).subscribe({
      next: (r) => {
        this.router.navigate(['/']);
      },
      error: (r) => {
        if (r.error?.message) {
          switch (r.error.message) {
            case "Account is disabled !":
              this.errorMessage = "La cuenta está deshabilitada.";
              break;
            case "Invalid credentials !":
              this.errorMessage = "Nombre de usuario o contraseña incorrectos.";
              break;
            default:
              this.errorMessage = "Error desconocido.";
          }
        } else {
          this.errorMessage = "Error desconocido.";
        }
      },
    });
  }
}

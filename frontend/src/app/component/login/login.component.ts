import {Component, OnInit} from "@angular/core";
import {LoginService} from "../../services/session.service";
import {Chart, registerables} from "chart.js";
import {Router} from "@angular/router";

Chart.register(...registerables);

@Component({
  selector: "app-login",
  templateUrl: "./login.component.html",
  styleUrls: ["./login.component.css", "../../../animations.css"],
})

export class LoginComponent implements OnInit {
  title = "Bookmarks";
  errorMessage: string | null = null;

  constructor(public loginService:LoginService, private router:Router) {
  }

  ngOnInit() {}

  logIn(userName:string, userPassword:string){
    this.loginService.login({username:userName, password:userPassword}).subscribe({
      next: r => {
        this.router.navigate(['/'])
      },
      error: r => {
        this.errorMessage = "Nombre de usuario o contraseña incorrectos";
      }
    });
  }
}

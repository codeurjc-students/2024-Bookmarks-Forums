import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { LoginService } from '../../services/session.service';
import { PostService } from '../../services/post.service';
import { UserService } from '../../services/user.service';
import { CommunityService } from '../../services/community.service';
import { Chart, registerables } from 'chart.js';
import { DatePipe } from '@angular/common';
import { TitleService } from '../../services/title.service';
Chart.register(...registerables);

@Component({
  selector: 'app-error',
  templateUrl: './error.component.html',
  styleUrls: ['./error.component.css', '../../../animations.css'],
  providers: [DatePipe],
})
export class ErrorComponent implements OnInit {
  errorTitle: string = 'Error';
  errorCode: number = 500;
  errorMessage: string =
    'Ha ocurrido un error inesperado. Por favor, inténtalo de nuevo más tarde.';

  constructor(
    private http: HttpClient,
    public loginService: LoginService,
    public profileService: UserService,
    public postService: PostService,
    public communityService: CommunityService,
    private route: ActivatedRoute,
    private titleService: TitleService
  ) {}

  ngOnInit(): void {
    this.titleService.setTitle('Error');
    this.route.queryParams.subscribe((params) => {
      if (!params['title'] && !params['description']) {
        this.errorTitle = 'Algo ha salido mal';
        this.errorMessage =
          'Si ves esta pantalla muy a menudo, por favor, contacta con nosotros';
      } else {
        this.errorTitle = params['title'];
        this.errorMessage = params['description'];
        this.errorCode = params['code'];
      }
    });
  }
}

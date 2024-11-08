import { trigger, transition, style, animate, query, group } from '@angular/animations';

export const slideInAnimation = trigger('slideInAnimation', [
  transition('LoginPage => SignupPage', [
    style({ position: 'relative' }),
    query(':enter, :leave', [
      style({
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%',
      })
    ], { optional: true }),
    group([
      query(':enter .form-card', [
        style({ opacity: 0, backdropFilter: 'blur(10px)' }),
        animate('500ms ease', style({ opacity: 1, backdropFilter: 'blur(10px)' }))
      ], { optional: true }),
      query(':leave .form-card', [
        style({ opacity: 1, backdropFilter: 'blur(10px)' }),
        animate('500ms ease', style({ opacity: 0, backdropFilter: 'blur(10px)' }))
      ], { optional: true }),
    ])
  ]),
  transition('SignupPage => LoginPage', [
    style({ position: 'relative' }),
    query(':enter, :leave', [
      style({
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%',
      })
    ], { optional: true }),
    group([
      query(':enter .form-card', [
        style({ opacity: 0, backdropFilter: 'blur(10px)' }),
        animate('500ms ease', style({ opacity: 1, backdropFilter: 'blur(10px)' }))
      ], { optional: true }),
      query(':leave .form-card', [
        style({ opacity: 1, backdropFilter: 'blur(10px)' }),
        animate('500ms ease', style({ opacity: 0, backdropFilter: 'blur(10px)' }))
      ], { optional: true }),
    ])
  ])
]);
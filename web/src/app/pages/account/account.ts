import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../features/auth/auth.service';
@Component({selector:'app-account',imports:[FormsModule],templateUrl:'./account.html'})
export class Account {
 private readonly auth=inject(AuthService); private readonly router=inject(Router); readonly mode=signal<'login'|'register'>('login'); email='';password='';displayName='';role: 'USER' | 'SCHOOL_ADMIN'='USER';error='';
 setMode(mode: 'login' | 'register'){this.mode.set(mode);this.error='';}
 submit(){this.error='';const email=this.email.trim();const displayName=this.displayName.trim();if(!email || !this.password){this.error='Podaj e-mail i hasło.';return;}if(this.mode()==='register'){if(!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)){this.error='Podaj poprawny adres e-mail.';return;}if(displayName.length<2){this.error='Imię musi mieć co najmniej 2 znaki.';return;}if(this.password.length<10){this.error='Hasło musi mieć co najmniej 10 znaków.';return;}}if(this.mode()==='login'){this.login();return;}this.auth.register$({email,password:this.password,displayName,role:this.role}).subscribe({next:()=>this.login(),error:e=>this.error=this.apiMessage(e)??'Nie udało się utworzyć konta.'});}
 private login(){this.auth.login$(this.email.trim(),this.password).subscribe({next:()=>this.router.navigateByUrl('/news'),error:()=>this.error='Nieprawidłowy e-mail lub hasło.'});}
 private apiMessage(error: any): string | null { const body=error.error; return body?.message ?? (body && typeof body==='object' ? Object.values(body)[0] as string : null); }
}

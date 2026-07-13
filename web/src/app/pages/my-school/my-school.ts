import { Component, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { School, SchoolPayload } from '../../features/schools/school.model';
import { SchoolService } from '../../features/schools/school.service';
import { AuthService } from '../../features/auth/auth.service';
@Component({selector:'app-my-school',imports:[FormsModule,RouterLink],templateUrl:'./my-school.html'})
export class MySchool {
 private readonly schools=inject(SchoolService); protected readonly auth=inject(AuthService); readonly mine=signal<School[]>([]); error=''; showCreateDialog=false; form:SchoolPayload={name:'',address:'',krs:null,description:'',activities:'',pricing:''};
 constructor(){effect(()=>{if(this.canManageSchools())this.load();else this.mine.set([]);});}
 canManageSchools(){const role=this.auth.currentUser()?.role;return role==='SCHOOL_ADMIN'||role==='SUPER_ADMIN';}
 load(){this.schools.mine$().subscribe({next:s=>this.mine.set(s),error:()=>this.error='Zaloguj się, aby zarządzać szkołą.'});}
 openCreateDialog(){if(!this.canManageSchools())return;this.error='';this.showCreateDialog=true;}
 closeCreateDialog(){this.showCreateDialog=false;}
 create(){if(!this.canManageSchools()){this.error='Tylko administrator szkoły może utworzyć szkołę.';return;}this.schools.create$(this.form).subscribe({next:s=>{this.mine.update(x=>[...x,s]);this.form={name:'',address:'',krs:null,description:'',activities:'',pricing:''};this.closeCreateDialog();},error:e=>this.error=e.error?.message??'Nie udało się utworzyć szkoły.'});}
}

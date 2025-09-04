import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastNotificationComponent } from './components/toast-notification.component';
import { AppInitService } from './services/app-init.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ToastNotificationComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  protected title = 'BlueWing Tax Portal';

  constructor(private appInitService: AppInitService) {}

  ngOnInit(): void {
    // Initialize the application
    this.appInitService.initializeApp().subscribe({
      next: (success) => {
        if (success) {
          console.log('Application initialized successfully');
          // Set up authentication state change handling
          this.appInitService.handleAuthStateChange();
        }
      },
      error: (error) => {
        console.error('Failed to initialize application:', error);
      },
    });
  }
}

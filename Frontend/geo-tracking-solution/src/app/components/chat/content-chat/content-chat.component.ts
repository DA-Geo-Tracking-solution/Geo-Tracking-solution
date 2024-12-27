import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';
import { ServerDataService } from '../../../services/server-data/server-data.service';
import { RestService } from '../../../services/REST/rest.service';
import { ChatMessage, Chat } from '../../../models/interfaces';
import { MessageService } from '../../../services/message/message.service';
import { KeycloakService } from '../../../services/keycloak/keycloak.service';



@Component({
  selector: 'app-content-chat',
  templateUrl: './content-chat.component.html',
  styleUrls: ['./content-chat.component.css']
})
export class ContentChatComponent implements OnChanges {
  @Input() chat!: Chat;
  @Input() currentTheme!: string;

  public messages: ChatMessage[] = [];
  public message: string = '';
  public navbarOpen: boolean = false;  // Burger-Menü Zustand

  constructor(private keycloakService: KeycloakService, private restService: RestService, private serverDataService: ServerDataService, private messageService: MessageService) { }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['chat']) {

      this.messages = [];
      this.messageService.getMessages(this.chat.chatId, (message: ChatMessage)=>{
        this.messages.push(message);
      });

      /*this.messages = [];
      this.serverDataService.getChatMessages(this.chat.chatId, (data: any) => {
        console.log(data);
        if (data.sender && data.content && data.timestamp) {
          this.messages.push(data);
        }
      });*/
    }
  }

  // Nachricht senden
  sendMessage(): void {
    if (this.message.trim()) {
      this.restService.POST(`member/chat/${this.chat.chatId}/message`, this.message).then(observable => {
        observable.subscribe({
          next: (line) => {
            console.log(line)
          },
          error: (err) => {
            console.error("Error in Observable:", err);
          },
          complete: () => {
            console.log("Observable completed");
          },
        });
      }).catch(err => {
        console.error("Error resolving promise:", err);
      });
      this.message = '';  // Eingabefeld nach dem Senden leeren
    }
  }

  get userEmail(): string | undefined {
    return this.keycloakService.user?.email;
  }

  // Burger-Menü Umschalten
  toggleNavbar(): void {
    this.navbarOpen = !this.navbarOpen;
  }
}

import { Injectable } from '@angular/core';
import { Chat, ChatMessage } from '../../models/interfaces';
import { ServerDataService } from '../server-data/server-data.service';

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  messages: Map<string, ChatMessage[]> = new Map();
  callbacks: Map<string, ((messages: ChatMessage) => void)> = new Map()  

  constructor(private serverDataService: ServerDataService) { }

  public init(chats: Chat[]): void {
    for (const chat of chats) {
      this.addChat(chat);
    }
  }

  public addChat(chat: Chat) {
    console.log(this.messages.get(chat.chatId))
    if (this.messages.get(chat.chatId) === undefined) {
      this.messages.set(`${chat.chatId}`,  []);
      console.log(chat)
      this.serverDataService.getChatMessages(chat.chatId, (data: any) => {
        console.log(data);
        if (data.authorEmail && data.content && data.key.timestamp) {
          this.messages.get(`${chat.chatId}`)?.push(data);
          const callback = this.callbacks.get(`${chat.chatId}`);
          console.log(chat.chatId)
          if (callback) {
            callback(data);
          }
        }
      });
    }
  }

  public initCallback(chatId: string, callback: (messages: ChatMessage) => void): void {
    const messages = this.messages.get(`${chatId}`) || [];
    for (const message of messages) {
      callback(message);
    }
    this.callbacks.set(`${chatId}`,  callback);
  }
}

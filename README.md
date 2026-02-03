# Firebase Chat - Android Application

A real-time chat application for Android built with Kotlin and Firebase. This application allows users to create accounts, participate in multiple chat rooms, and send messages in real-time.

## 📱 Features

- **User Authentication**: Secure user registration and login using Firebase Authentication
- **Real-time Messaging**: Send and receive messages instantly using Firebase Realtime Database
- **Multiple Chat Rooms**: Create and participate in multiple group conversations
- **Participant Management**: Add participants to existing chat rooms
- **Chat Room Management**: Create new chat rooms with custom names
- **Message History**: View complete message history with timestamps
- **User Presence**: See who is participating in each chat room
- **Persistent Sessions**: Stay logged in across app restarts using SharedPreferences
- **Delete Chat Rooms**: Long-press to delete chat rooms (with confirmation)

## 🛠️ Technologies Used

- **Language**: Kotlin
- **Architecture**: MVVM pattern with data binding
- **UI Framework**: Android Views with ViewBinding
- **Backend Services**:
  - Firebase Authentication (user management)
  - Firebase Realtime Database (data storage and synchronization)
- **Minimum SDK**: API 31 (Android 12)
- **Target SDK**: API 35
- **Build System**: Gradle with Kotlin DSL

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- Android Studio (latest version recommended)
- JDK 11 or higher
- Android SDK with API 31+ support
- A Firebase project configured for Android

## 🚀 Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/jrgim/App24-25-FirebaseChat.git
cd App24-25-FirebaseChat
```

### 2. Firebase Configuration

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select an existing one
3. Add an Android app to your Firebase project
4. Register your app with the package name: `es.usj.jglopez.firebasechat`
5. Download the `google-services.json` file
6. Place the `google-services.json` file in the `app/` directory

### 3. Enable Firebase Services

In the Firebase Console, enable the following services:

#### Firebase Authentication
- Navigate to Authentication > Sign-in method
- Enable **Email/Password** authentication

#### Firebase Realtime Database
- Navigate to Realtime Database
- Create a database in **test mode** (or configure security rules as needed)
- Your database structure will be:
  ```
  {
    "users": {
      "userId": {
        "name": "string",
        "createdAt": timestamp,
        "chatrooms": { "chatroomId": boolean }
      }
    },
    "chatrooms": {
      "chatroomId": {
        "id": "string",
        "name": "string",
        "participants": { "username": boolean },
        "messages": [
          {
            "senderName": "string",
            "messageText": "string",
            "timestamp": timestamp
          }
        ],
        "createdBy": "string",
        "createdAt": timestamp,
        "lastMessage": "string"
      }
    }
  }
  ```

### 4. Build and Run

1. Open the project in Android Studio
2. Let Gradle sync the dependencies
3. Connect an Android device or start an emulator (API 31+)
4. Click **Run** or press `Shift + F10`

## 📖 Application Structure

### Screens

1. **SplashScreen**: Initial screen that checks if user is logged in
   - Redirects to MainActivity if user session exists
   - Redirects to RegisterUser if no session found

2. **RegisterUser**: Authentication screen
   - Login with existing credentials
   - Create new account automatically if username doesn't exist
   - Uses email format: `username@example.com` for simplified authentication

3. **MainActivity**: Main chat list view
   - Displays all chat rooms the user is participating in
   - Shows last message preview for each chat
   - Click to open a chat room
   - Long-press to delete a chat room
   - Floating action buttons for:
     - Creating new chat rooms
     - Logging out

4. **CreateChat**: Create new chat room
   - Enter chat room name
   - Select participants from list of registered users
   - Creates initial message automatically

5. **chatScreen**: Individual chat room view
   - Display all messages in chronological order
   - Send new messages
   - Add more participants via floating action button
   - Real-time message updates
   - Messages from current user appear on the right
   - Messages from others appear on the left

6. **AddParticipants**: Add users to existing chat room
   - Select users from list
   - Add them to the current chat room

### Data Models

- **User**: User profile information
  - `name`: Username
  - `createdAt`: Account creation timestamp
  - `chatrooms`: Map of chat room memberships

- **chatroom**: Chat room information
  - `id`: Unique identifier
  - `name`: Chat room name
  - `participants`: Map of participating users
  - `messages`: List of messages
  - `createdBy`: Creator username
  - `createdAt`: Creation timestamp
  - `lastMessage`: Preview of last message

- **message**: Individual message
  - `senderName`: Username of sender
  - `messageText`: Message content
  - `timestamp`: Message timestamp

### Key Components

- **CheckboxAdapter**: RecyclerView adapter for user selection with checkboxes
- **SharedPreferences**: Local storage for user session persistence
- **Firebase Realtime Database Listeners**: Real-time synchronization of messages and chat rooms

## 🔐 Security Notes

- The app currently uses simplified email format (`username@example.com`) for authentication
- Users only need to remember username and password
- Session data is stored locally using SharedPreferences
- For production use, consider implementing:
  - Proper Firebase Security Rules
  - Input validation and sanitization
  - Rate limiting
  - User profile pictures
  - End-to-end encryption for messages

## 🎨 User Interface

The application uses Material Design components with:
- RecyclerView for efficient list rendering
- ViewBinding for type-safe view access
- Material Floating Action Buttons
- Constraint Layout for flexible UI design
- Dark theme support

## 📝 Dependencies

Key dependencies used in this project:

```kotlin
// Core Android
androidx.core:core-ktx
androidx.appcompat:appcompat
com.google.android.material:material
androidx.activity:activity
androidx.constraintlayout:constraintlayout

// Firebase
com.google.firebase:firebase-bom
com.google.firebase:firebase-auth
com.google.firebase:firebase-firestore
com.google.firebase:firebase-database

// JSON Parsing
com.google.code.gson:gson

// Testing
junit:junit
androidx.test.ext:junit
androidx.test.espresso:espresso-core
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is created for educational purposes at Universidad San Jorge (USJ).

## 👨‍💻 Authors

Developed for USJ (2024-2025) by:
- **jglopez** - Project lead and main developer
- **Arksuga02** (Arkaitz Subias Garcia) - Contributor
- **lamari777** (Álex Langarita) - Contributor
- **LilSamu** (Samuel Orgaz) - Contributor
- **777ruslanski** (Pablo Tudela) - Contributor

## 📞 Support

If you encounter any issues or have questions, please open an issue in the GitHub repository.

## 🔄 Future Enhancements

Potential features for future development:

- [ ] Profile pictures for users
- [ ] Image and file sharing in chats
- [ ] Push notifications for new messages
- [ ] Message read receipts
- [ ] Typing indicators
- [ ] Search functionality for messages
- [ ] Chat room avatars
- [ ] User online/offline status
- [ ] Message reactions (emoji)
- [ ] Voice messages
- [ ] Video calls
- [ ] Admin roles for chat rooms
- [ ] Message editing and deletion
- [ ] Dark/Light theme toggle

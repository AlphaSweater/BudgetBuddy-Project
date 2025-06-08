
![Logo](https://i.postimg.cc/VN8BpwF0/Synaptix-Logo.png)


# BudgetBuddy App - PROG7313

BudgetBuddy is a user-friendly personal budget tracker app designed to help individuals take control of their finances with ease. Whether you're looking to manage your daily expenses, track multiple sources of income, or simply gain better insights into your spending habits, BudgetBuddy makes the process effective and simple. The app allows users to easily track their income and expenses, categorize transactions, and monitor their finances through easy-to-read summaries and reports. With its clean and modern interface, BudgetBuddy offers an intuitive experience suitable for both beginners and experienced users alike. By providing monthly breakdowns, income vs. expense comparisons, and insightful summaries, the app incourages users to make informed financial decisions and develop better money management habits over time. Whether your goal is to save more, reduce unnecessary spending, or plan for the future. BudgetBuddy is your companion on your journey to financial stability and success.


## Features

- SignUp and Login: Users can securely create an account and log in to access their personal budget data from anywhere. This ensures that all financial information is protected and personalized for each user.

- Create Categories and Entries: Users can easily organize their finances by creating custom categories (e.g., Food, Transport, Entertainment) and adding income or expense entries under each category. This helps users track where their money is going in a structured way.

- Take and Store Photos: Users can capture and attach photos of receipts, invoices, or any related documents to your entries. This feature helps users keep a visual record of their transactions for better tracking and accountability.

- Set Minimum and Maximum Goals: Set financial goals by setting minimum and maximum spending limits. This allows users to stick to their budget and avoid overspending.

- View List of Entries in a Period: View a detailed list of all income and expense entries within a selected date range. This makes it easy to review and analyze user financial activity over days, weeks, or months.

- View Category Totals in a Period: Get a clear summary of total spending and earnings per category within a specific time period. This feature provides insights into user's spending habits and helps adjust their budget as needed.


## Own features

- Multi-Wallet support: Application allows for multiple different wallets to be created and used by one profile, each wallet has its own transactions and balance as well as their own individual minimum and maximum goals that gets added together to create a global minimum and maximum goal for the user.

- Advanced multi-Budget Support: 
## Prerequisites 

1. **Ensure that you have Android Studio downloaded or you won't be able to run the project.**
Ensure that you have Android Studio installed on your computer.

2. If you do not have Android Studio, you can download it here:
👉 https://developer.android.com/studio

3. Make sure you have the following installed within Android Studio:
- Android SDK 26+
- Gradle 8.0+

4. Install an Android emulator or use a real device for testing.
## How to Compile and Run The Application

1. Download and install Android Studio (Giraffe or newer) from the official site:
https://developer.android.com/studio

2. Open Android Studio on your computer.

3. Get the project files:

- Option 1 - Clone the Repository: Click "Get from Version Control" in the github repository and paste the repository link:
https://github.com/AlphaSweater/BudgetBuddy-Project.git

- Option 2 - Download Zip File: If you downloaded a ZIP file, extract it, then click "Open" in Android Studio and select the extracted project folder.

- Wait for Gradle sync to complete. Android Studio will automatically download required dependencies.
(This may take a few minutes the first time.)

- Ensure you have the correct SDK versions installed:

- Minimum SDK version needed is 25 (35+ recommended)
(You can check or install SDKs via SDK Manager in Android Studio.)

4. Connect a device to run the app:

- Option 1: Plug in a physical Android device with USB debugging enabled.

- Option 2: Create and start an Android Emulator via Device Manager in Android Studio.

5. Click the green "Run" button at the top (or press Shift + F10) to build and launch the app.

6. The app will install and launch on your selected device, showing the BudgetBuddy application.


## YouTube Video Demonstration

Below is the link for our YouTube video:

- https://www.youtube.com/watch?v=SFQ90baMvlU
## Tech Stack
### Core

- 100% [Kotlin](https://kotlinlang.org/)
- 100% [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material3 design](https://m3.material.io/) (UI components)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) (structured concurrency)
- [Kotlin Flow](https://kotlinlang.org/docs/flow.html)
- [Hilt](https://dagger.dev/hilt/) (DI)

### Database
- [Firestore](https://console.firebase.google.com) (firebase no-sql database)
- [Imgur API](https://console.firebase.google.com) (Image uploading and storage)

### Build & CI
- [Gradle KTS](https://docs.gradle.org/current/userguide/kotlin_dsl.html)
- [Gradle version catalogs](https://developer.android.com/build/migrate-to-catalogs) (dependencies versions)
- [GitHub Actions](https://github.com/Ivy-Apps/ivy-wallet/actions)



## Screenshots

![App Screenshot](https://via.placeholder.com/468x300?text=App+Screenshot+Here)


## Database Implementation

![App Screenshot](https://via.placeholder.com/468x300?text=App+Screenshot+Here)
## contributors
<a href="https://github.com/AlphaSweater/BudgetBuddy-Project/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=AlphaSweater/BudgetBuddy-Project" />
</a>

- Made with [contrib.rocks](https://contrib.rocks).

- Chad Fairlie ST10269509
- Dhiren Ruthenavelu ST10256859
- Kayla Ferreira ST10259527
- Nathan Teixeira ST10249266


## References

- https://www.youtube.com/watch?v=A_tPafV23DM&list=PLPgs125_L-X9H6J7x4beRU-AxJ4mXe5vX
- https://www.geeksforgeeks.org/kotlin-android-tutorial/
- https://www.geeksforgeeks.org/textview-in-kotlin/
- https://www.geeksforgeeks.org/scrollview-in-android/
- https://www.geeksforgeeks.org/horizontalscrollview-in-kotlin/
- https://www.geeksforgeeks.org/cardview-in-android-with-example/
- https://www.geeksforgeeks.org/switch-in-kotlin/
- https://www.geeksforgeeks.org/spinner-in-kotlin/
- ChatGPT was used to help with the design and planning. As well as assisted with finding and fixing errors in the code.
- ChatGPT also helped with the forming of comments for the code.

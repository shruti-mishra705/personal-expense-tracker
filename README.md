# Personal Expense Tracker

A simple, user-friendly Android app to track daily expenses, view summaries, and manage your personal finances.

## Features

- **Add, Edit, Delete Expenses:** Record expenses with category, amount, date, and time.
- **Expense Summary:** View total, average, and number of expenses, plus your salary and savings.
- **Pie Chart Visualization:** See your spending breakdown by category.
- **Database Storage:** All data is stored locally using Room (built on SQLite).
- **Intuitive Navigation:** Easily switch between main and summary screens.
- **Customizable UI:** Colored buttons, card views, and a responsive layout.
- **Custom App Icon:** Professional launcher icon for easy identification.

## Getting Started

### Prerequisites

- Android Studio (latest version recommended)
- Android device or emulator (API 21+)

## Project Structure

- `MainActivity.kt` – Main screen for listing and managing expenses.
- `DetailsActivity.kt` – Summary and visualization of expenses.
- `ExpenseDatabase.kt` – Room database setup.
- `ExpenseDao.kt` – Data access object for expenses.
- `Expense.kt` – Data model (Room entity).
- `res/layout/` – All UI layouts.
- `res/drawable/` – App icons and vector images.
- `res/values/` – Colors, strings, and styles.

## Tech Stack

- **Kotlin**
- **Room Database (SQLite)**
- **MPAndroidChart** (for pie chart)
- **Android Jetpack Components**

## Customization

- **Change App Icon:**  
Replace the image in `res/mipmap-*` using Android Studio’s Image Asset tool.
- **Add Categories:**  
Edit the `expense_categories` array in `res/values/strings.xml`.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Credits

- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) for charting library.
- [Material Icons](https://fonts.google.com/icons) for vector assets.

---
**Feel free to fork, contribute, and suggest improvements!**

![WhatsApp Image 2025-05-25 at 22 38 47](https://github.com/user-attachments/assets/b5ccc75f-56f5-41ee-b972-8b23180b42cc)
![WhatsApp Image 2025-05-25 at 22 38 46](https://github.com/user-attachments/assets/b0265312-ba10-4912-9af5-8841e062230e)





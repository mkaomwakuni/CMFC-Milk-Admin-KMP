# CMFC Milk Admin -KMP

CMFC Milk Admin is a comprehensive milk cooperative management system that supports Android, iOS,
Web & Desktop. By using CMFC Milk Admin, you can manage cooperative members, track livestock health,
record daily milk production, handle sales transactions, monitor inventory levels, generate
financial reports, and export data in multiple formats.

## Design Concept

Built from the concept by [Khairuzzaman Konok](https://dribbble.com/khairuzzaman-konok) on Dribbble

![Design Concept](https://cdn.dribbble.com/userupload/17172177/file/original-4abde46ed0612a0cea5bb752e19774ce.jpg?resize=1200x853&vertical=center)

## Desktop Screenshots

![Dashboard](composeApp/src/commonMain/composeResources/assests/dash.png)
![Milk Entry](composeApp/src/commonMain/composeResources/assests/entry.png)
![Members](composeApp/src/commonMain/composeResources/assests/memeberentries.png)
![Cows Management](composeApp/src/commonMain/composeResources/assests/cows.png)
![Cow Metrics](composeApp/src/commonMain/composeResources/assests/cowmetrics.png)
![Milk Out Sales](composeApp/src/commonMain/composeResources/assests/milkout.png)
![Stock Management](composeApp/src/commonMain/composeResources/assests/stock.png)
![Spoilt Milk](composeApp/src/commonMain/composeResources/assests/spoilt.png)
![Export Reports](composeApp/src/commonMain/composeResources/assests/export.png)
![Settings](composeApp/src/commonMain/composeResources/assests/settings.png)

## Future Plans

- Real-time notifications and alerts
- Mobile app synchronization
- Advanced analytics and ML insights
- Integration with payment gateways
- Multi-language support
- Cloud backup and restore

## 🌟 Contributions

If you wanna contribute, Please make sure to add new features & Then make a PR. Feel free to
contribute to the project and stay tuned for more exciting updates!

## Open To Work

Do you wanna Convert your thoughts into Physical & Successful Project Then I'm here for you. I'm
open to work, available for Freelance or Remote Work Opportunities. Feel free to reach me out on
Email.

## 🤝 Connect with Me

Let's chat about potential projects, job opportunities, or any other collaboration! Feel free to
connect with me through the following channels:

[LinkedIn](https://linkedin.com/in/mkao)

## 💰 You can help me by Donating

[BuyMeACoffee](https://buymeacoffee.com/earl89) |
## Setup

To use this Admin Panel, You need to setup the CMFC Milk Server first. After setting up the server,
You need to get the local ip from your terminal using Command Prompt or Terminal. You just need to
replace your ip with BASE_URL inside the
`composeApp/src/commonMain/kotlin/cnc/coop/milkcreamies/core/constants/AppConstants.kt`

This is a Kotlin Multiplatform project targeting Android, Desktop, Web.

`/composeApp` is for code that will be shared across your Compose Multiplatform applications. It
contains several subfolders:

- `commonMain` is for code that's common for all targets.
- Other folders are for Kotlin code that will be compiled for only the platform indicated in the
  folder name.

Learn more
about [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html), [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform), [Kotlin/Wasm](https://kotl.in/wasm/)...

**Note:** Compose/Web is Experimental and may be changed at any time. Use it only for evaluation
purposes. We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack
channel #compose-web. If you face any issues, please report them on GitHub.

You can open the web application by running the `:composeApp:wasmJsBrowserDevelopmentRun` Gradle
task.


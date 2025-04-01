# 🥛 Chonyi Milk Cooperative Management System

A comprehensive digital solution for managing milk cooperative operations, built with **Kotlin
Multiplatform** and **Compose Multiplatform**. This system streamlines the management of cooperative
members, livestock, milk production tracking, sales, and financial records.

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Compose%20Multiplatform-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Desktop](https://img.shields.io/badge/Desktop-Windows%20%7C%20macOS%20%7C%20Linux-blue?style=for-the-badge)

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Technology Stack](#-technology-stack)
- [Getting Started](#-getting-started)
- [Project Structure](#-project-structure)
- [Key Modules](#-key-modules)
- [Screenshots](#-screenshots)
- [API Integration](#-api-integration)
- [Contributing](#-contributing)
- [License](#-license)

## ✨ Features

### 👥 Member Management

- **Member Registration**: Add and manage cooperative members
- **Member Profiles**: Detailed member information and statistics
- **Member Performance**: Track individual member milk production metrics
- **Archive System**: Handle member suspension, departure, or relocation
- **Search & Filter**: Find members quickly with advanced filtering options

### 🐄 Livestock Management

- **Cow Registration**: Complete cow profiles with breed, age, weight, and health status
- **Health Monitoring**: Track vaccination schedules, treatments, and health conditions
- **Milk Eligibility**: Smart system to determine cow eligibility for milk collection
- **Archive Management**: Handle cow sales, deaths, or other status changes
- **Owner Association**: Link cows to their respective members

### 🥛 Milk Production Tracking

- **Daily Milk Collection**: Record morning and evening milk production
- **Smart Validation**: Prevent milk collection from unhealthy or archived cows
- **Real-time Inventory**: Track current milk stock levels
- **Production Analytics**: Detailed production reports and trends
- **Quality Control**: Handle spoilt milk reporting with loss calculations

### 💰 Sales & Financial Management

- **Milk Sales**: Record daily milk sales to customers
- **Customer Management**: Maintain customer database
- **Payment Tracking**: Support for cash and M-Pesa payments
- **Financial Reports**: Comprehensive earnings and expense tracking
- **Profit Analysis**: Calculate daily, weekly, and monthly profits

### 📊 Analytics & Reporting

- **Dashboard Overview**: Real-time operational metrics
- **Production Charts**: Visual representation of milk production trends
- **Member Statistics**: Individual member performance metrics
- **Export Functionality**: Export data in CSV, Excel, and PDF formats
- **Historical Data**: Complete audit trail of all operations

### 🗄️ Archive System

- **Smart Archiving**: Automatic cow dissociation when members are archived
- **Archive Reasons**: Categorized archiving (suspended, left cooperative, sold, deceased)
- **Archive Viewer**: Dedicated interface to view and filter archived items
- **Data Preservation**: All historical data remains accessible
- **Restoration Support**: Architecture supports data restoration if needed

## 🏗️ Architecture

The application follows **MVVM (Model-View-ViewModel)** architecture with **Clean Architecture**
principles:

```
├── 📱 Presentation Layer (UI/ViewModels)
├── 🏢 Domain Layer (Use Cases/Repositories)
├── 🔧 Data Layer (Remote API/Local Storage)
└── 🛠️ Core Layer (Utilities/Constants)
```

### Key Architectural Decisions

- **Kotlin Multiplatform**: Share business logic across platforms
- **Compose Multiplatform**: Native UI performance with shared UI code
- **Repository Pattern**: Clean separation of data sources
- **State Management**: Reactive programming with Kotlin Flow
- **Dependency Injection**: Koin for clean dependency management

## 🛠️ Technology Stack

### Frontend

- **Kotlin Multiplatform Mobile (KMM)**
- **Compose Multiplatform** - UI framework
- **Material Design 3** - Modern UI components
- **Kotlin Coroutines** - Asynchronous programming
- **Kotlin Flow** - Reactive data streams

### Backend Integration

- **Ktor Client** - HTTP client for API communication
- **Kotlinx Serialization** - JSON serialization/deserialization
- **REST API** - Communication with backend services

### Architecture & Patterns

- **MVVM Architecture** - Separation of concerns
- **Repository Pattern** - Data access abstraction
- **Koin** - Dependency injection
- **Clean Architecture** - Maintainable and testable code

### Data Management

- **Real-time Inventory Management**
- **Local Caching** - Offline capability
- **Data Validation** - Business rule enforcement
- **Export Utilities** - Multi-format data export

## 🚀 Getting Started

### Prerequisites

- **JDK 11** or higher
- **Android Studio** (latest stable version)
- **Kotlin** 1.9.0+
- **Gradle** 7.6+

### Installation

1. **Clone the repository**

```bash
git clone https://github.com/yourusername/chonyi-milk-cooperative.git
cd chonyi-milk-cooperative
```

2. **Open in Android Studio**
    - Import the project in Android Studio
    - Let Gradle sync complete
    - Ensure all dependencies are downloaded

3. **Configure API Endpoint**
    - Update `AppConstants.Api.BASE_URL` in the core module
    - Set your API key in `AppConstants.Api.API_KEY`

4. **Run the Application**
    - **Android**: Select Android target and run
    - **Desktop**: Select Desktop target and run
    - **iOS**: (Future support) Select iOS target and run

### Environment Setup

Create a `local.properties` file in the root directory:

```properties
sdk.dir=/path/to/android/sdk
api.base.url=https://your-api-endpoint.com
api.key=your-api-key-here
```

## 📁 Project Structure

```
composeApp/
├── src/
│   ├── commonMain/kotlin/cnc/coop/milkcreamies/
│   │   ├── 🎨 presentation/          # UI Layer
│   │   │   ├── ui/screens/          # Screen implementations
│   │   │   ├── ui/components/       # Reusable UI components
│   │   │   └── viewmodel/           # ViewModels for each feature
│   │   ├── 🏢 domain/               # Business Logic Layer
│   │   │   ├── repository/          # Repository interfaces
│   │   │   └── usecase/             # Business use cases
│   │   ├── 🔧 data/                 # Data Layer
│   │   │   ├── remote/              # API clients
│   │   │   └── repository/          # Repository implementations
│   │   ├── 📊 models/               # Data models
│   │   ├── 🛠️ core/                 # Core utilities
│   │   ├── 🔌 di/                   # Dependency injection
│   │   └── 🧭 navigation/           # Navigation logic
│   ├── androidMain/                 # Android-specific code
│   ├── desktopMain/                 # Desktop-specific code
│   └── iosMain/                     # iOS-specific code (future)
```

## 🔧 Key Modules

### 👥 Members Module

- Member registration and profile management
- Performance tracking and analytics
- Archive functionality with cow dissociation
- Search and filtering capabilities

### 🐄 Cows Module

- Comprehensive livestock management
- Health monitoring and treatment tracking
- Milk eligibility validation
- Archive management for sales/deaths

### 🥛 Milk Production Module

- Daily milk collection tracking
- Real-time inventory management
- Smart validation and quality control
- Production analytics and reporting

### 💰 Sales Module

- Customer management
- Sales transaction recording
- Payment method tracking
- Financial analytics

### 📊 Dashboard Module

- Real-time operational overview
- Key performance indicators
- Quick access to critical functions
- Visual data representation

## 📱 Screenshots

*Screenshots will be added here showcasing the key features of the application*

### Dashboard

- Overview of daily operations
- Key metrics at a glance
- Quick action buttons

### Member Management

- Member list with search functionality
- Individual member profiles
- Archive management interface

### Livestock Management

- Cow registration and profiles
- Health status tracking
- Milk eligibility indicators

### Milk Production

- Daily milk collection interface
- Real-time inventory tracking
- Production analytics

## 🔗 API Integration

The application integrates with a REST API for data management:

### Key Endpoints

- `GET /members` - Retrieve all members
- `POST /members` - Add new member
- `POST /members/{id}/archive` - Archive member
- `GET /cows` - Retrieve all cows
- `POST /cows/{id}/archive` - Archive cow
- `POST /milk-in` - Record milk collection
- `GET /milk-analytics` - Get production analytics

### Authentication

- API key-based authentication
- Configurable timeout settings
- Error handling and retry logic

## 🤝 Contributing

We welcome contributions to improve the Milk Cooperative Management System!

### How to Contribute

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Make your changes**
    - Follow the existing code style
    - Add tests for new functionality
    - Update documentation as needed
4. **Commit your changes**
   ```bash
   git commit -m 'Add some amazing feature'
   ```
5. **Push to the branch**
   ```bash
   git push origin feature/amazing-feature
   ```
6. **Open a Pull Request**

### Development Guidelines

- Follow Kotlin coding conventions
- Write meaningful commit messages
- Add unit tests for new features
- Update documentation for API changes
- Ensure UI consistency with Material Design 3

### Code Style

- Use meaningful variable and function names
- Comment complex business logic
- Follow the established architecture patterns
- Keep functions small and focused

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

```
Copyright 2025 MkaoCodes

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 📞 Support

For support, please open an issue on GitHub or contact the development team.

### Reporting Issues

- Use the GitHub issue tracker
- Provide detailed reproduction steps
- Include relevant logs and screenshots
- Tag issues appropriately

### Feature Requests

- Describe the feature in detail
- Explain the use case and benefits
- Consider the impact on existing functionality

---

**Built with ❤️ for the Chonyi Milk Cooperative Community**

*This project aims to digitize and streamline milk cooperative operations, making them more
efficient and transparent for all stakeholders.*

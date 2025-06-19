# 🥛CMFC-Milk-Admin-KMP

This is a simple desktop application made for managing day-to-day activities in a milk cooperative. 
It helps track members, livestock, milk production, sales, and financial records,built with **Kotlin
Multiplatform** and **Compose Multiplatform**. This system streamlines the management of cooperative
members, livestock, milk production tracking, sales, and financial records.

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Compose%20Multiplatform-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Desktop](https://img.shields.io/badge/Desktop-Windows%20%7C%20macOS%20%7C%20Linux-blue?style=for-the-badge)


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
- **Financial Reports**: Comprehensive earnings and expense tracking
- **Profit Analysis**: Calculate daily, weekly, and monthly profits

### 📊 Analytics & Reporting

- **Dashboard Overview**: Real-time operational metrics
- **Production Charts**: Visual representation of milk production trends
- **Member Statistics**: Individual member performance metrics
- **Export Functionality**: Export data in CSV, Excel, and PDF formats

### 🗄️ Archive System

- **Smart Archiving**: Automatic cow dissociation when members are archived
- **Archive Reasons**: Categorized archiving
- **Archive Viewer**: Dedicated interface to view and filter archived items
- **Data Preservation**: All historical data remains accessible

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
git clone https://github.com/mkaomwakuni/CMFC-Milk-Admin-KMP.git
cd CMFC-Milk-Admin-KMP
```

2. **Open in Android Studio**
    - Import the project in Android Studio
    - Let Gradle sync complete
    - Ensure all dependencies are downloaded

3. **Configure API Endpoint**
    - Update `AppConstants.BASE_URL` in the core module

4. **Run the Application**
    - **Desktop**: Select Desktop target and run

### Environment Setup

Create a `local.properties` file in the root directory:

```properties
sdk.dir=/path/to/android/sdk
api.base.url=  http://localhost:8081/

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

*Screenshots coming soon*

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

### Key Endpoints - For CMFCL Server KMP

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

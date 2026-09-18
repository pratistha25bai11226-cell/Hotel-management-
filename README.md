# EduLodge – Campus Hostel & Mess Management System

EduLodge is a console-based Java application designed to simplify day-to-day hostel administration for educational institutions. It provides a single platform for managing student accommodation, mess (dining hall) attendance, and billing — replacing manual, paper-based record keeping with a structured, role-based system.

The application supports two types of users, **Students** and **Admin Wardens**, each with their own dedicated menu and set of permissions.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Usage Guide](#usage-guide)
- [Data Persistence](#data-persistence)
- [Default Admin Account](#default-admin-account)
- [Limitations](#limitations)
- [Future Improvements](#future-improvements)
- [License](#license)

---

## Features

### For Students
- Self-registration with name, email, roll number, and password
- Secure login
- View personal profile and assigned room details
- View monthly mess bill (days attended × daily rate)
- Generate and download a printable mess invoice (`.txt` file)

### For Admin Wardens
- View all hostel rooms with block, capacity, and current occupancy
- Allocate a student to an available room
- Log a student's mess attendance and daily rate for a given month
- View system-wide room and billing data

---

## Tech Stack

- **Language:** Java (JDK 8+)
- **Persistence:** Java Object Serialization (flat `.dat` files — no external database required)
- **Security:** SHA-256 password hashing
- **Interface:** Command-line (CLI), using `java.util.Scanner`

No external libraries or build tools are required — the entire application runs from standard Java SE.

---

## Architecture

EduLodge follows a layered design, all contained within a single file for simplicity:

```
Presentation Layer  →  MainApp (CLI menus)
Service Layer       →  AuthService, AllocationService, BillingService
Data Access Layer   →  GenericDAO<T, ID> / FileGenericDAO<T, ID>
Data Model Layer     →  User, Student, AdminWarden, Room, MessRecord
```

- **Data Models** – `User` is an abstract base class extended by `Student` and `AdminWarden`, demonstrating inheritance and polymorphism via `displayProfile()`.
- **DAO Layer** – A generic, file-backed DAO (`FileGenericDAO`) implements `save`, `findById`, `findAll`, and `delete`, reused across all four entity types.
- **Service Layer** – Encapsulates business logic: authentication, room allocation rules, and billing calculations.
- **Presentation Layer** – A simple text-based menu system routes users to role-specific sub-menus after login.

---

## Project Structure

```
MainApp.java        # Single-file application containing all classes
users.dat           # Serialized user accounts (auto-generated on first run)
students.dat        # Serialized student records (auto-generated)
rooms.dat           # Serialized room records (auto-generated)
mess.dat            # Serialized mess attendance/billing records (auto-generated)
Invoice_*.txt        # Generated invoices (created when a student exports a bill)
```

---

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 8 or later installed
- A terminal or command prompt

### Compile

```bash
javac MainApp.java
```

### Run

```bash
java MainApp
```

On first launch, the system automatically creates a default admin account and two sample rooms (see [Default Admin Account](#default-admin-account) below).

---

## Usage Guide

1. **Launch the application** — you'll see the main menu with three options: Register Student, User Login, and Exit.
2. **Register** a new student account, or **log in** with existing credentials (student or admin).
3. **Students** can view their profile, check mess bills, and export invoices from the Student Portal.
4. **Admin Wardens** can view rooms, allocate students to rooms, and log mess attendance from the Admin Portal.
5. Choose the logout option in either portal to return to the main menu, or select Exit to close the application.

---

## Data Persistence

EduLodge does not use a database. Instead, each data type (users, students, rooms, mess records) is stored in its own `.dat` file using Java's built-in object serialization:

- Data is loaded into memory when the application starts.
- Every save or delete operation immediately re-serializes the affected dataset back to disk.
- Files are created automatically in the working directory on first use — no manual setup is required.

---

## Default Admin Account

On the very first run, the system seeds a default warden account and two starter rooms if none exist:

| Field | Value |
|---|---|
| User ID | `ADMIN01` |
| Password | `admin123` |
| Assigned Block | A-Block |
| Rooms Created | A-101, A-102 (capacity: 2 each) |

> ⚠️ **Security note:** Change or remove this default password before using the system beyond local testing/demo purposes.

---

## Limitations

- Single-user, single-machine CLI application — not designed for concurrent multi-user access over a network.
- Student records are stored in two separate DAOs (`users.dat` and `students.dat`), which can drift out of sync if extended carelessly.
- No input validation on numeric fields (e.g., non-numeric input for days attended or daily rate will crash the program).
- Password hashing uses unsalted SHA-256, which is not recommended for production security standards.

---

## Future Improvements

- Migrate persistence from serialized files to a relational database (e.g., SQLite or MySQL)
- Add salted, adaptive password hashing (bcrypt/argon2)
- Add robust input validation and error handling throughout the CLI
- Build a graphical or web-based interface on top of the existing service layer
- Add automated tests for the service and DAO layers
- Support room de-allocation and student transfers between rooms

---

## License

This project is provided as-is for educational purposes. Feel free to modify and extend it for coursework, learning, or personal projects.

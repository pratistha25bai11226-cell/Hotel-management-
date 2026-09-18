*3. Problem Statement: `statement.md`**

```markdown
# Project Problem Statement: EduLodge Hostel & Mess Management System

## 1. Background & Context
Educational institutions frequently struggle with manually managing student accommodation allocations, tracking occupancy limits across hostel blocks, and processing monthly mess billing records. Traditional spreadsheet-based approaches are prone to human error, data redundancy, and lack proper role-based access security.

## 2. Objective
The objective of this project is to design, develop, and deploy **EduLodge**, an Object-Oriented Java software system that automates core hostel administration workflows, enforces data integrity via standard software architecture, and maintains data persistence without relying on external SQL server setups[cite: 1].

## 3. Scope of Work
The system encompasses four core domain modules implemented across 11 internal classes[cite: 1]:

1. **User Management & Authentication**:
   * Secure registration and authentication for Students and Warden Administrators[cite: 1].
   * Cryptographic password hashing (SHA-256) for secure credential verification[cite: 1].
2. **Room & Block Allocation**:
   * Management of room inventory across hostel blocks with defined capacity thresholds[cite: 1].
   * Dynamic student assignment preventing room over-allocation[cite: 1].
3. **Mess Attendance & Billing**:
   * Logging student monthly attendance days and daily rates[cite: 1].
   * Automated total fee calculation and invoice generation exported to local text files[cite: 1].
4. **Data Persistence (DAO Layer)**:
   * Implementation of a Generic DAO interface using Java Object Serialization to auto-save system states to binary `.dat` files upon every transaction[cite: 1].

## 4. Technical Constraints & Deliverables
* **Language**: Core Java (JDK 8+)[cite: 1]
* **Architecture**: Multi-layered separation of Model, Data Access (DAO), Service, and Interface layers[cite: 1].
* **Module Requirement**: Minimum of 5 to 10 logical classes/modules integrated into a clean layout[cite: 1].
* **Version Control**: Public GitHub repository containing complete source code, compilation instructions, and documented project statements[cite: 1].
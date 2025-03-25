# Pitch: A Social Music Review App 

## Overview
**Pitch** is a Kotlin-based Android social media app inspired by social review apps Letterboxd, designed for music enthusiasts to review, rate, and discover songs. Users can share their thoughts, explore trending music, and connect with others through a personalized and dynamic feed.
---

### **Database**
- **PostgreSQL**: Relational database for structured storage of user profiles, reviews, song metadata, and social features (followers, likes).
- **Tables**: `users`, `reviews`, `songs`, `followers`, etc.

### **API**
- **Spotify API**: Access to 100M+ songs, albums, and artists for search, metadata, and recommendations.
- **Google OAuth**: Secure user authentication and profile management.

### **Sensors**
- **Camera**: Upload profile pictures.
- **Accelerometer**: "Shake-to-Review" feature for spontaneous criticism.

### **Target Devices**
- **Mobile Phones**: Primary interface for on-the-go reviews and social interactions.
- **Tablets**: Responsive layouts with expanded song details and side-by-side feeds.

---

## Core Functionality
1. **User Authentication**: Google OAuth signup/login.
2. **Profile Management**: Bio, profile pic, follower count, and review history.
3. **Song Search & Review**:
    - Search via Spotify API.
    - Rate songs (1-5 stars) and write reviews.
    - Like the reviews of other users, and follow those whose musings you enjoy the most.
4. **Social Feed**: Trending reviews, followed users’ activity.
6. **Export**: Export review cards as stories on social media platforms like Instagram, Snapchat, and Facebook. (stretch goal).

---

## Project Timeline
**Deadline #1 (Due 3/25)**
- [x] Proposal & README
- [x] Slide deck (concept, tech stack, wireframes)
- [x] GitHub repo setup with initial Kotlin project
- [ ] Class presentation

---
 
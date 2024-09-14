<h1 align="center">    
Bookmarks Forums    
</h1>  


<h2 align="left">    
Table of contents    
</h2>  

[TOC]

## 1. About this Web Application

- **Web Application's name:** Bookmarks Forums.
- **Author:** Izan Ruiz Ballesteros
- **Tutors:** Óscar Soto Sánchez, Micael Gallego Carrillo



## 2. Medium blog link

This project has a blog posted in Medium. Click [here](https://medium.com/@izanrb) to visit it!



## 3. GitHub Project link

Click [here](https://github.com/codeurjc-students/2024-Bookmarks-Forums) if you want to visit the GitHub Project.



## 4. What this does

Bookmarks is an already existing web application where users could search for books and mark them as **currently reading**, **already read** and **want to read**.

Bookmarks Forums is the update that brings the social to that application!

With Bookmarks Forums users will be able to create content about their favorite authors or books, follow other users, join and create communities and meet new people!



## 5. Other details

Here are the technical aspects of the web application described:



### 5.1. Entities

This web application has the following entities in its database:

- User: users database not shared with Bookmarks.
- Post: publications made by the users.
- Reply: short text, image, or mix between those as a response to an existing post.
- Community: groups of users, posts and post replies.
- Message: messages sent between users.
- Chat: group of messages between two users.

The users database is separated from Bookmarks to let the users decide whether they want to participate in the forums or not, regardless of having a Bookmarks account.



### 5.2. Permissions per kind of user

This table defines the permissions each kind of user has over which entity:

| Kind of user                | Permissions                                                  | Owner of                              |
| --------------------------- | ------------------------------------------------------------ | ------------------------------------- |
| Visitor (unregistered user) | Visit the landing page, search for forum posts, visit profiles, visit posts, visit communities. | N/A                                   |
| Registered user             | All the above plus participate in chats, join communities, create communities, join communities, modify communities, ban users from communities, create forum posts, modify posts, delete posts (only the ones they are owners of), reply to other users' posts, like posts, dislike posts, create chat with other users, send messages to other user (using a chat). | Post, Community, Message, Chat, User. |
| Administrator               | All the above plus modify and delete all communities, modify and delete all posts, ban users from site, access the administrator zone. | All                                   |

⚠️ **NOTE:** Registered users can **only** modify communities and ban users from communities they are **owners** of.



### 5.3. Images' info

These entities have images linked to them:

* Community: communities have an icon.
* User: users have profile pictures.
* Post: a post may have multiple images.
* Reply: a reply may contain multiple images.



### 5.4. Graphs and charts

- Users with the most liked content: shown in the landing page as a bar chart.
- Rate of users registered per day: shown as a line chart in the administrator zone.



### 5.5. Advanced complementary technology

The aim is to include the following additional technologies to the project:

- Mail notifications will be sent to users that have been directly messaged or replied.
- Websockets will be used in order to offer real-time notifications for chat messages.



### 5.6. Other algorithms

The landing page will show users the most popular posts based on how many likes they have and the most recent ones.

Users will have the ability to search for communities, posts or users.

Users will be able to sort posts by date (from most recent to least recent) or likes inside communities and users' profiles.



### 5.7. Extras

Apart from the main functionality and technologies mentioned above, the web application will contain:

- Automatic tests: for unit integration testing.
- Packaging: as GraalVM native images.
- Deployment: on a cloud VM.



## 6. Wireframe

Below is the list of screens the web application has:

- Login Page
- Sign-up Page
- Landing page.
- Community page.
- Community creation and modification page
- Post page.
- Post creation and modification page.
- User profile page.
- Chat page.
- Search results page.
- Administrator zone.
- Error page.



And here is the navigation map:





**NOTE**: The Search Results Page is accessible through the search bar located in the application's navigation bar at the top of the screen, which is visible in all pages except the Login and Sign-up pages.

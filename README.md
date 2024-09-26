<h1 style="text-align: center;">
Bookmarks Forums    
</h1>  


<h2 style="text-align: left;">
Table of contents    
</h2>  

1. [About this Web Application](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#1-about-this-web-application)
2. [Medium blog link](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#2-medium-blog-link)
3. [GitHub Project link](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#3-github-project-link)
4. [What this does](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#4-what-this-does)
5. [Other details](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#5-other-details)
   1. [Entities](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#i-entities)
   2. [Permissions per kind of user](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#ii-permissions-per-kind-of-user)
   3. [Images' info](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#iii-images-info)
   4. [Graphs and charts](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#iv-graphs-and-charts)
   5. [Advanced complementary technology](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#v-advanced-complementary-technology)
   6. [Other algorithms](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#vi-other-algorithms)
   7. [Extras](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#vii-extras)
6. [Wireframe](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#6-wireframe)

## 1. About this Web Application

- **Web Application's name:** Bookmarks Forums.
- **Author:** Izan Ruiz Ballesteros
- **Tutors:** Óscar Soto Sánchez, Micael Gallego Carrillo



## 2. Medium blog link

This project has a blog posted in Medium. Click [here](https://medium.com/@izanrb) to visit it!



## 3. GitHub Project link

Click [here](https://github.com/codeurjc-students/2024-Bookmarks-Forums) if you want to visit the GitHub Project.



## 4. What this does

### Basic functionality

Bookmarks is an already existing web application where users could search for books and mark them as **currently reading**, **already read** and **want to read**.

Bookmarks Forums is the update that brings the social to that application!

With Bookmarks Forums any user will be able to sign up or login to the forums, create content about their favorite authors or books, follow other users, join and create communities and meet new people!

### Advanced functionality

Apart from what has been mentioned above, Bookmarks Forums will allow set search sorting filters for searching the most popular or recent posts.

Users will be able to create chat rooms to chat between each other.

The main landing page will serve posts and content using an algorithm based on the popularity of posts.

The application will offer charts that will allow users and administrators to easily check the most popular users and more.

Users will receive emails regarding messages or replies to their posts.

The application will serve a REST API.

More details on these advanced technologies are described below.


## 5. Other details

Here are the technical aspects of the web application described:



### i. Entities

This web application has the following entities in its database:

- User: users database not shared with Bookmarks.
- Post: publications made by the users.
- Reply: short text, image, or mix between those as a response to an existing post.
- Community: groups of users, posts and post replies.
- Message: messages sent between users.
- Chat: group of messages between two users.

The users database is separated from Bookmarks to let the users decide whether they want to participate in the forums or not, regardless of having a Bookmarks account.

![ER DB Diagram](https://github.com/user-attachments/assets/b09bc8af-5a64-46e9-a440-86be834c69c7)


### ii. Permissions per kind of user

This table defines the permissions each kind of user has over which entity:

| Kind of user                | Permissions                                                                                                                                                                                                                                                                                                                                                           | Owner of                              |
|-----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------|
| Visitor (unregistered user) | Visit the landing page, search for forum posts, visit profiles, visit posts, visit communities.                                                                                                                                                                                                                                                                       | N/A                                   |
| Registered user             | All the above plus participate in chats, join communities, create communities, join communities, modify communities, ban users from communities, create forum posts, modify posts, delete posts (only the ones they are owners of), reply to other users' posts, like posts, dislike posts, create chat with other users, send messages to other user (using a chat). | Post, Community, Message, Chat, User. |
| Administrator               | All the above plus modify and delete all communities, modify and delete all posts, ban users from site, access the administrator zone.                                                                                                                                                                                                                                | All                                   |

⚠️ **NOTE:** Registered users can **only** modify communities and ban users from communities they are **owners** of.



### iii. Images' info

These entities have images linked to them:

* Community: communities have an icon.
* User: users have profile pictures.
* Post: a post may have multiple images.
* Reply: a reply may contain multiple images.
* Message: a message may contain an image.



### iv. Graphs and charts

- Users with the most liked content: shown in the landing page as a bar chart.
- Rate of users registered per day: shown as a line chart in the administrator zone.



### v. Advanced complementary technology

The aim is to include the following additional technologies to the project:

- Mail notifications will be sent to users that have been directly messaged or replied.
- Websockets will be used in order to offer real-time notifications for chat messages.

These are considered part of the advanced functionalities of the application.


### vi. Other algorithms

The landing page will show users the most popular posts based on how many likes they have and the most recent ones.

Users will have the ability to search for communities, posts or users.

Users will be able to sort posts by date (from most recent to least recent) or likes inside communities and users' profiles.



### vii. Extras

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

![Bookmarks Forums Navigation Map](https://github.com/user-attachments/assets/64abd20b-b7b3-431e-a35f-6a121ddf0c87)

⚠️ **NOTE**: The Search Results Page is accessible through the search bar located in the application's navigation bar at the top of the screen, which is visible in all pages except the Login and Sign-up pages.

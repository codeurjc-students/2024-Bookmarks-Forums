![Application render](<Readme images/Main.png>)

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
   1. [Entities and ER diagram](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#i-entities-and-er-diagram)
   2. [Backend diagram](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#ii-backend-diagram)
   3. [Frontend (SPA) diagram](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#iii-frontend-spa-diagram)
   4. [Permissions per kind of user](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#iv-permissions-per-kind-of-user)
   5. [Images' info](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#v-images-info)
   6. [Graphs and charts](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#vi-graphs-and-charts)
   7. [Advanced complementary technology](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#vii-advanced-complementary-technology)
   8. [Other algorithms](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#viii-other-algorithms)
   9. [Extras](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#ix-extras)
6. [Wireframe](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#6-wireframe)
7. [Screenshots](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#7-screenshots)
8. [Current navigation map](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#8-current-navigation-map)
9. [REST API Documentation](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#9-rest-api-documentation)
10. [Docker instructions for building the application's image](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#10-docker-instructions-for-building-the-applications-image)
11. [Docker instructions for running the application's container](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#11-docker-instructions-for-running-the-applications-container)
12. [Demonstrative video](https://github.com/codeurjc-students/2024-Bookmarks-Forums?tab=readme-ov-file#12-demonstrative-video)

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

Administrators will be able to see the worst behaving users and ban them from the site.

❗**The chats and advanced administrator functionalities will be implemented in the v1.0 release of the application.**

## 5. Other details

Here are the technical aspects of the web application described:

### i. Entities and ER diagram

For the current version of the application, it has the following entities in its database:

- User: the users of the application.
- Post: publications made by the users.
- Reply: short text as a response to an existing post.
- Community: groups of users, posts and post replies.
- Ban: a user can be banned from a community. This entity is used to store the ban information.

The users database is separated from Bookmarks to let the users decide whether they want to participate in the forums or not, regardless of having a Bookmarks account.

![Database Entity Relationship Diagram](<Readme images/ER DB Diagram v2.png>)

### ii. Backend diagram

The backend of the application is structured as follows:

![Backend Class Diagram](<Readme images/Backend Diagram.png>)

- ![#808080](https://via.placeholder.com/15/808080/000000?text=+) **Grey**: entities.
- ![#99c8ff](https://via.placeholder.com/15/99c8ff/000000?text=+) **Blue**: repositories.
- ![#fe7070](https://via.placeholder.com/15/fe7070/000000?text=+) **Red**: services.
- ![#fddf71](https://via.placeholder.com/15/fddf71/000000?text=+) **Yellow**: REST controllers.
- ![#7ed886](https://via.placeholder.com/15/7ed886/000000?text=+) **Green**: other controllers.

### iii. Frontend (SPA) diagram

The frontend of the application is structured as follows:

![Frontend Class Diagram](<Readme images/Frontend Diagram.png>)

- ![#eab6ff](https://via.placeholder.com/15/eab6ff/000000?text=+) **Purple**: components.
- ![#99c8ff](https://via.placeholder.com/15/99c8ff/000000?text=+) **Blue**: services.
- ![#fe7070](https://via.placeholder.com/15/fe7070/000000?text=+) **Red**: templates.
- ![#7ed886](https://via.placeholder.com/15/7ed886/000000?text=+) **Green**: directives.

### iv. Permissions per kind of user

This table defines the permissions each kind of user has over which entity:

| Kind of user                | Permissions                                                                                                                                                                                                                                         | Owner of               |
| --------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------- |
| Visitor (unregistered user) | Visit the landing page, search for forum posts, visit profiles, visit posts, visit communities.                                                                                                                                                     | N/A                    |
| Registered user             | All the above plus join communities, create communities, modify communities, ban users from communities, create forum posts, modify posts, delete posts (only the ones they are owners of), reply to other users' posts, like posts, dislike posts. | Post, Community, User. |
| Administrator               | All the above plus modify and delete all communities, modify and delete all posts.                                                                                                                                                                  | All                    |

⚠️ **NOTE:** Registered users can **only** modify communities and ban users from communities they are **owners** of.

### v. Images' info

These entities have images linked to them:

- Community: communities have an icon.
- User: users have profile pictures.
- Post: a post may have multiple images.

### vi. Graphs and charts

- Users with the most liked content: shown in the landing page as a bar chart.
- Rate of users registered per day: shown as a line chart in the administrator zone. (This will be implemented in the future as part of the administrator page, with its advanced functionalities).

### vii. Advanced complementary technology

The aim is to include the following additional technologies to the project:

- Mail notifications will be sent to users that have been directly messaged or replied.
- Websockets will be used in order to offer real-time notifications for chat messages. (This will be implemented in the future as part of the chat functionality).

### viii. Other algorithms

The landing page will show users the most popular posts based on how many likes they have and the most recent ones.

Users will have the ability to search for communities, posts or users.

Users will be able to sort posts by date (from most recent to least recent) or likes inside communities and users' profiles.

### ix. Extras

Apart from the main functionality and technologies mentioned above, the web application will contain:

- Automatic tests: for unit integration testing.
- Packaging: as GraalVM native images.
- Deployment: on a cloud VM.

## 6. Wireframe

Below is the list of screens the web application will have:

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

And here is the wireframe:

![Bookmarks Forums Navigation Map](https://github.com/user-attachments/assets/64abd20b-b7b3-431e-a35f-6a121ddf0c87)

⚠️ **NOTE**: The Search Results Page is accessible through the search bar located in the application's navigation bar at the top of the screen, which is visible in all pages except the Login and Sign-up pages.

## 7. Screenshots

Here are some screenshots of the web application, showcasing the different pages the current version of the application has:

### Login page:

![Login Screen](<Readme images/Screenshots/Login_Screen.png>)
_Login screen for user authentication._

### Sign-up page:

![Sign-up Screen](<Readme images/Screenshots/Signup_Screen.png>)
_Sign-up screen for new users._

### Landing page:

![Landing Screen](<Readme images/Screenshots/Landing_Screen.png>)
_Main landing page after login._

### Community page:

![Community Screen](<Readme images/Screenshots/Community_Screen.png>)
_Community page showing posts and members._

### New/Edit Community page:

![New/Edit Community Screen](<Readme images/Screenshots/New_Community_Screen.png>)
_Page for creating or editing a community._

### Post page:

![Post Screen](<Readme images/Screenshots/Post_Screen.png>)
_Detailed view of a single post._

### New/Edit Post page:

![New/Edit Post Screen](<Readme images/Screenshots/New_Post_Screen.png>)
_Page for creating or editing a post._

### User profile page:

![User Profile Screen](<Readme images/Screenshots/Profile_Screen.png>)
_User profile page displaying user information._

### Modify User page:

![Modify User Screen](<Readme images/Screenshots/Modify_profile_Screen.png>)
_Page for modifying user profile details._

### Search results page:

![Search Results Screen](<Readme images/Screenshots/Search_page_Screen.png>)
_Search results page showing search outcomes._

### Error page:

![Error Screen](<Readme images/Screenshots/Error_Screen.png>)
_Error page displayed for invalid actions or pages._

# 8. Current navigation map

Here is the navigation map of the web application. The map shows the different screens and the possible transitions between them per user permissions.

In this map, the chat and administrator zone are not shown as they will be implemented in the future as advanced functionalities.

![Navigation Map](<Readme images/Nav Diagram.png>)

# 9. REST API Documentation

The documentation for the REST API has been generated using Swagger and the OpenAPI specification.
The YAML file can be found in the following link: [api-docs-yaml](backend/api-docs/api-docs.yaml)
However, a more visual and interactive version can be found in the following link: [API Docs HTML Version](https://raw.githack.com/codeurjc-students/2024-Bookmarks-Forums/refs/heads/main/backend/api-docs/api-docs.html)

# 10. Docker instructions for building the application's image

⚠️ **In order to build the application's Docker image, you need to have Docker, Node.js and Angular CLI installed on your machine.**

To build the application's Docker image, follow these steps:

1. Clone the repository to your local machine:

```bash
git clone https://github.com/codeurjc-students/2024-Bookmarks-Forums.git
```

2. Login to your Docker account:

```bash
docker login
```

3. Go to the project's docker directory:

```bash
cd 2024-Bookmarks-Forums/docker
```

4. Run the following automated script to build the Docker image:

On Linux:

```bash
./create_image.sh
```

[create_image.sh](https://github.com/codeurjc-students/2024-Bookmarks-Forums/blob/main/docker/create_image.sh)

On Windows:

```bash
create_image.bat
```

[create_image.bat](https://github.com/codeurjc-students/2024-Bookmarks-Forums/blob/main/docker/create_image.bat)

This script will do the following:

- Build the Angular project.
- Copy the Angular project's build files to the backend's resources directory.
- Build the backend project with Maven.
- Build the Docker image with the backend's JAR file and the Dockerfile.
- Push the Docker image to DockerHub.
- Set up the Docker container with the application's image.


# 11. Docker instructions for running the application's container

⚠️ **In order to run the application's Docker container, you need to have Docker installed on your machine.**

To run the application's Docker container, follow these steps:

1. Clone the repository to your local machine:

```bash
git clone https://github.com/codeurjc-students/2024-Bookmarks-Forums.git
```

2. Go to the project's docker directory:

```bash
cd 2024-Bookmarks-Forums/docker
```

3. Run one of these docker-compose commands to start the application's container:

On Linux:

```bash
sudo docker compose -p bookmarks-forums up --build
```

```bash
sudo docker compose up
```

On Windows:

```bash
docker-compose -p bookmarks-forums up --build
```

```bash
docker-compose up
```

4. Access the application in your browser at the following URL:

```bash
https://localhost:443
```

# 12. Demonstrative video

Here is a video showcasing the application's main functionalities (spanish):

[![Demonstrative Video](https://img.youtube.com/vi/Z09aqRIdNzs/0.jpg)](https://youtu.be/Z09aqRIdNzs)

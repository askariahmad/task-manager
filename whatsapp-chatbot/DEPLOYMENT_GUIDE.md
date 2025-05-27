# Deployment Guide (Heroku Example)

This guide provides basic steps to deploy the WhatsApp Chatbot (with MongoDB and Dialogflow integration) to Heroku.

## Prerequisites
1.  A Heroku account (free tier available).
2.  [Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli) installed and authenticated (`heroku login`).
3.  [Git](https://git-scm.com/downloads) installed.
4.  **Twilio Account:** SID, Auth Token, and a Twilio WhatsApp-enabled phone number.
5.  **MongoDB Database:** A MongoDB instance. A free tier is available from [MongoDB Atlas](https://www.mongodb.com/cloud/atlas/register).
6.  **Google Cloud Project with Dialogflow:** As set up according to `NLP_SETUP_GUIDE.md`. This includes:
    *   Google Cloud Project ID.
    *   Dialogflow Agent created.
    *   Service Account JSON key file for Dialogflow API access.

## Build Instructions
1.  Navigate to the project's root directory (where `pom.xml` is located).
2.  Build the executable JAR file using Maven:
    ```bash
    mvn clean package
    ```
    This will create the JAR file in the `target/` directory (e.g., `target/whatsapp-chatbot-0.0.1-SNAPSHOT.jar`).

## Heroku Deployment Steps

1.  **Initialize Git Repository (if not already):**
    ```bash
    git init
    git add .
    git commit -m "Initial commit for Heroku deployment"
    ```

2.  **Create a Heroku App:**
    ```bash
    heroku create your-unique-app-name
    ```
    (If you omit `your-unique-app-name`, Heroku will generate one for you). This also adds a `heroku` git remote.

3.  **Set Up MongoDB Atlas (Example):**
    *   Sign up/log in to [MongoDB Atlas](https://cloud.mongodb.com/).
    *   Create a new project and a new cluster (the M0 Sandbox tier is free).
    *   Once the cluster is provisioned:
        *   **Database Access:** Create a database user (e.g., `chatbot_user` with a secure password).
        *   **Network Access:** Add your current IP address for testing. For Heroku, you'll need to allow access from all IP addresses (`0.0.0.0/0`) or find Heroku's outbound IP range (which can be dynamic and complex, so `0.0.0.0/0` is common for free tiers but less secure for production).
        *   **Get Connection String:** Click "Connect" for your cluster, choose "Connect your application", select "Java" as the driver, and copy the connection string (SRV address). It will look something like `mongodb+srv://<username>:<password>@clustername.mongodb.net/<dbname>?retryWrites=true&w=majority`. Replace `<username>`, `<password>`, and optionally `<dbname>` (e.g., `whatsapp_chatbot_db`).

4.  **Set Environment Variables on Heroku:**
    Replace placeholders with your actual credentials and values.

    *   **Twilio Credentials:**
        ```bash
        heroku config:set TWILIO_ACCOUNT_SID="your_twilio_account_sid"
        heroku config:set TWILIO_AUTH_TOKEN="your_twilio_auth_token"
        heroku config:set TWILIO_PHONE_NUMBER="whatsapp:+your_twilio_whatsapp_number"
        ```

    *   **MongoDB URI:**
        Use the connection string from MongoDB Atlas. Make sure to URL-encode any special characters in the username or password if necessary.
        ```bash
        heroku config:set MONGODB_URI="mongodb+srv://chatbot_user:your_password@yourcluster.mongodb.net/whatsapp_chatbot_db?retryWrites=true&w=majority"
        ```

    *   **Dialogflow Credentials:**
        ```bash
        heroku config:set DIALOGFLOW_PROJECT_ID="your-google-cloud-project-id"
        ```
        For the `GOOGLE_APPLICATION_CREDENTIALS` (Service Account JSON key):
        Heroku doesn't directly support uploading JSON key files to be referenced by path in the same way as local development. The recommended way is to store the *content* of the JSON key in a Heroku config variable.
        1.  Open your downloaded service account JSON key file.
        2.  Copy the entire JSON content (it's a single line or can be made into one).
        3.  Set it as a Heroku config var:
            ```bash
            # Ensure the JSON is properly escaped if entered directly on the command line,
            # or paste it carefully when prompted by `heroku config:edit` or via Heroku dashboard.
            # It's often easier to paste multi-line JSON via the Heroku Dashboard (Settings > Reveal Config Vars).
            heroku config:set GOOGLE_CREDENTIALS_JSON_CONTENT='{"type": "service_account", "project_id": "...", ...}'
            ```
            **Important:** The current `DialogflowService.java` expects `GOOGLE_APPLICATION_CREDENTIALS` to be a file path. To use `GOOGLE_CREDENTIALS_JSON_CONTENT`, `DialogflowService` would need to be modified to initialize `SessionsClient` using `ServiceAccountCredentials.fromStream()` with an `InputStream` derived from this JSON string. This modification is NOT yet in the current codebase.

            **Alternative for `GOOGLE_APPLICATION_CREDENTIALS` (if file path is strictly needed, more complex for Heroku):**
            This would involve using a custom buildpack or a more complex setup to get the file onto the Heroku dyno. For simplicity, the JSON content method is preferred if the Java code is adapted. *Since the code is not yet adapted, this guide will mention the current limitation.*

            **Current Limitation:** The application's `DialogflowService` currently expects `GOOGLE_APPLICATION_CREDENTIALS` as a file path. For Heroku, you'd ideally modify the service to load credentials from the JSON string stored in `GOOGLE_CREDENTIALS_JSON_CONTENT`. If you must use a file path on Heroku, it requires more advanced techniques not covered here. For local testing, set `GOOGLE_APPLICATION_CREDENTIALS` to the path of your JSON key file.

    *   **Java Options (Optional):**
        ```bash
        heroku config:set JAVA_OPTS="-Xmx512m"
        ```

5.  **Deploy to Heroku:**
    Push your code to the Heroku remote:
    ```bash
    git push heroku master 
    # Or main, if your default branch is main: git push heroku main
    ```

6.  **Check Application Logs:**
    ```bash
    heroku logs --tail
    ```
    Look for messages indicating Spring Boot has started, connected to MongoDB, and your application is running. Check for any Dialogflow authentication errors.

7.  **Configure Twilio Webhook:**
    *   Once deployed, Heroku will give you a URL for your app (e.g., `https://your-unique-app-name.herokuapp.com`).
    *   In your Twilio console, navigate to your WhatsApp sender's configuration.
    *   Set the webhook URL for "WHEN A MESSAGE COMES IN" to `https://your-unique-app-name.herokuapp.com/api/whatsapp/webhook` (using POST method).

## Important Notes
*   **Dialogflow Agent:** Ensure your Dialogflow agent is fully configured with intents and entities as described in `NLP_SETUP_GUIDE.md`.
*   **Security:** Be cautious with database access rules (MongoDB Atlas Network Access) and keep all credentials secure.
*   **Heroku Free Tier:** Free dynos sleep after inactivity. The first request after sleep will be slower. MongoDB Atlas M0 tier is also limited.
*   **Troubleshooting:** Use `heroku logs --tail` extensively to diagnose issues.

---
This guide provides a starting point. Cloud platform features and best practices evolve, so always refer to the official documentation for Heroku, MongoDB Atlas, and Google Cloud.
```

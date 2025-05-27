# NLP Setup Guide (Google Dialogflow)

This guide outlines the steps to set up Google Dialogflow for NLP integration with the WhatsApp Chatbot.

## 1. Google Cloud Project Setup
1.  **Create or Select a Project:**
    *   Go to the [Google Cloud Console](https://console.cloud.google.com/).
    *   If you don't have a project, create one. Give it a meaningful name (e.g., `whatsapp-chatbot-project`).
2.  **Enable Billing:** Dialogflow usage may incur costs beyond the free tier. Ensure billing is enabled for your project. New accounts often get free credits.
3.  **Enable Dialogflow API:**
    *   In the Google Cloud Console, navigate to "APIs & Services" > "Library".
    *   Search for "Dialogflow API" and enable it for your project.

## 2. Create a Dialogflow ES Agent
*Note: Dialogflow has two main versions: ES (Essentials) and CX (Customer Experience). This guide assumes Dialogflow ES, which is suitable for many chatbot applications.*

1.  **Go to Dialogflow ES Console:**
    *   Navigate to [Dialogflow ES Console](https://dialogflow.cloud.google.com/).
2.  **Create a New Agent:**
    *   Click "Create Agent" (or "Create new agent" if you have existing ones).
    *   **Agent Name:** e.g., `WhatsAppChatbotAgent`
    *   **Default Language:** e.g., `English - en`
    *   **Default Time Zone:** Select your relevant time zone.
    *   **Google Project:** Link it to the Google Cloud project you created/selected earlier.
    *   Click "Create".

## 3. Define Intents
Intents represent what your users want to do. Create the following intents in your Dialogflow agent:

*   **`greeting`**:
    *   **Training Phrases:** "Hi", "Hello", "Hey", "Good morning"
    *   **Responses (Text Response):** "Hello! How can I help you today?", "Hi there!"
*   **`goodbye`**:
    *   **Training Phrases:** "Bye", "Goodbye", "See you later"
    *   **Responses:** "Goodbye!", "Talk to you later!"
*   **`fallback`**: (This is usually a default intent, customize its responses if needed)
    *   **Responses:** "Sorry, I didn't understand that. Can you try rephrasing?", "I'm not sure how to help with that."

*   **Feature-Specific Intents (Examples - you'll need to define more):**

    *   **`add_todo`**:
        *   **Training Phrases:**
            *   "add buy milk to my to-do list" (annotate "buy milk" as `@sys.any` entity named `task_description`)
            *   "new todo: resolve bug report" (annotate "resolve bug report" as `task_description`)
            *   "remind me to buy groceries" (This might be better as a `set_reminder` intent, but shows flexibility. If using for todo, annotate "buy groceries" as `task_description`)
        *   **Action and parameters:** `action: addTodo`, parameter `task_description`
        *   **Responses:** (Optional, can be handled by your Java code) "OK, I've added [task_description] to your to-do list."

    *   **`list_todos`**:
        *   **Training Phrases:** "show my todos", "what's on my to-do list?", "list all tasks"
        *   **Action and parameters:** `action: listTodos`

    *   **`set_reminder`**:
        *   **Training Phrases:**
            *   "remind me to call John tomorrow at 5pm" (annotate "call John" as `@sys.any` named `task_description`, "tomorrow at 5pm" as `@sys.date-time` named `datetime`)
            *   "set a reminder for project meeting on Friday 10am" (annotate "project meeting" as `task_description`, "Friday 10am" as `datetime`)
        *   **Action and parameters:** `action: setReminder`, parameters `task_description`, `datetime`
        *   **Entities:** You will use built-in system entities like `@sys.any` for general text and `@sys.date-time` for dates and times. You might need to create custom entities for specific identifiers if `@sys.any` is too broad or not specific enough for things like `task_identifier`.

**For each functional intent (`add_todo`, `list_todos`, `set_reminder`, etc., as per the full list in the main plan):**
1.  Click the "+" next to "Intents" in the Dialogflow console.
2.  Give the intent a name (e.g., `add_todo`).
3.  Add diverse **Training Phrases** that users might say.
4.  In the "Action and parameters" section, define an **action name** (e.g., `addTodoAction`). This action name will be sent to your webhook.
5.  Identify **Parameters** within your training phrases. Highlight parts of the phrases and assign them to system entities (e.g., `@sys.any`, `@sys.date-time`, `@sys.number`) or custom entities you define. Give parameters meaningful names (e.g., `task_description`, `reminder_time`). Mark as "Required" if necessary.
6.  **(Optional) Fulfillment:** For now, you can leave "Enable webhook call for this intent" **disabled**. Your Java code will handle the logic after getting the intent. If you enable it, Dialogflow can call your backend directly. We will handle fulfillment in our Java code by interpreting the detected intent.
7.  Save the intent.

**(Refer to the plan for the full list of intents and suggested entities to create: `complete_todo`, `remove_todo`, `add_note`, `list_notes`, `find_note`, `delete_note`, `create_counter`, `increment_counter`, `decrement_counter`, `show_counter`, `delete_counter`, `list_reminders`, `delete_reminder`.)**

## 4. Authentication (Service Account)
Your Java application needs credentials to securely access your Dialogflow agent.

1.  **Go to Google Cloud Console > IAM & Admin > Service Accounts.**
2.  Select your project.
3.  Click **"+ CREATE SERVICE ACCOUNT"**.
4.  **Service account name:** e.g., `dialogflow-client-service-account`
5.  **Role:** Assign the role "Dialogflow API Client" (or "Dialogflow API Admin" if it needs to modify the agent, but Client is safer for just detection). You can find this under "Dialogflow" roles.
6.  Click **"DONE"**.
7.  Find the created service account in the list. Click the three dots (Actions) next to it and select **"Manage keys"**.
8.  Click **"ADD KEY" > "Create new key"**.
9.  Choose **JSON** as the key type and click **"CREATE"**. A JSON file will be downloaded. This is your service account key. **Keep it secure!**

## 5. Environment Variables for Java App
Your Spring Boot application needs to know your Dialogflow Project ID and the path to the service account key.

*   **`DIALOGFLOW_PROJECT_ID`**: This is the Project ID of your Google Cloud project (visible in the Google Cloud Console).
*   **`GOOGLE_APPLICATION_CREDENTIALS`**: This should be the **absolute path** to the downloaded JSON service account key file on the machine where your Java app will run.
    *   **Local Development:** Set this environment variable in your IDE's run configuration or your system's environment variables.
    *   **Heroku/Cloud Deployment:**
        *   For `GOOGLE_APPLICATION_CREDENTIALS`, a common practice for Heroku is to copy the *content* of the JSON key file and set it as a single environment variable (e.g., `GOOGLE_CREDENTIALS_JSON`). Your Java app would then need to load this string at startup and use it to authenticate, instead of relying on a file path.
        *   Alternatively, for platforms that support file storage, you'd upload the JSON key file and set `GOOGLE_APPLICATION_CREDENTIALS` to its path on the server. *The `DialogflowService.java` provided in the subtask implicitly uses the `GOOGLE_APPLICATION_CREDENTIALS` environment variable pointing to a file path. This is standard for Google Cloud SDKs.*

**Example for `DialogflowService.java` (if loading JSON content from env var):**
If you store the JSON content in an env var like `GOOGLE_CREDENTIALS_JSON`, `DialogflowService` would need to be modified to initialize `SessionsClient` using these credentials programmatically, rather than relying on the `GOOGLE_APPLICATION_CREDENTIALS` file path. (This is an advanced setup not covered by the current stub). For now, assume `GOOGLE_APPLICATION_CREDENTIALS` points to the file path.

---
After completing these steps, your Dialogflow agent will be set up, and your Java application will have the necessary configuration placeholders to connect to it. The next step in the plan will be to implement the Java code that uses this service to interpret user messages.

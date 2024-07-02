## Data Structure
1. users
   - id: long
   - email: varchar
   - name: varchar
   - password: varchar
   - created_at: timestamp
   - updated_at: timestamp
2. todos
   - id: long
   - uuid: varchar
   - name: varchar
   - description: text
   - due_date: timestamp
   - status: short
     - 0: not started
     - 1: in progress
     - 2: completed
   - is_deleted: bool
   - priority: int
   - created_at: timestamp
   - updated_at: timestamp
3. todos_users
   - id: long
   - todo_id: long
   - user_id: long
   - role: short
     - 0: owner
     - 1: collaborator
   - created_at: timestamp
   - updated_at: timestamp
4. activity_logs
   - id: long
   - todo_id: long
   - user_id: long
   - action: short
     - 0: create
     - 1: update
     - 2: delete
   - created_at: timestamp
   - updated_at: timestamp
5. users_activity_logs
   - todo_id: long
   - user_id: long
   - created_at: timestamp
   - updated_at: timestamp
6. tags
   - id: long
   - name: varchar
   - created_at: timestamp
   - updated_at: timestamp
7. todos_tags
    - id: long
    - todo_id: varchar
    - tag_id: varchar
    - created_at: timestamp
    - updated_at: timestamp

## API Endpoints
1. Users
   - POST /users
   - GET /users/{id}
   - PUT /users/{id}
2. Login
   - POST /login
3. Logout
   - POST /logout
4. Todos
   - POST /todos
   - GET /todos
   - GET /todos/{id}
   - PUT /todos/{id}
   - DELETE /todos/{id}
5. Tags
   - POST /tags
   - GET /tags
   - GET /tags/{id}
6. Activity Logs
   - POST /activity_logs
   - GET /activity_logs
   - GET /activity_logs/{id}
# Supabase Integration for E-SIMAKSI

This project includes a global Supabase connection setup for easy database operations.

## Files Created

1. `app/src/main/java/com/zawww/e_simaksi/api/SupabaseConfig.kt` - Configuration class for Supabase
2. `app/src/main/java/com/zawww/e_simaksi/api/SupabaseClient.kt` - Global Supabase client as singleton
3. `app/src/main/java/com/zawww/e_simaksi/EsimaksiApplication.kt` - Application class with Supabase initialization
4. `app/src/main/java/com/zawww/e_simaksi/api/SupabaseUsageExample.kt` - Example usage of the Supabase connection

## Setup Instructions

1. **Get your Supabase credentials**:
   - Go to your Supabase project dashboard
   - Navigate to Project Settings > API
   - Copy the Project URL and Anon Key (public)

2. **Update the string resources**:
   - Open `app/src/main/res/values/strings.xml`
   - Replace the placeholder values for `supabase_url` and `supabase_anon_key` with your actual credentials

## Dependencies

The following Supabase dependencies are already included in `app/build.gradle.kts`:
- `implementation(platform("io.github.jan-tennert.supabase:bom:2.4.1"))`
- `implementation("io.github.jan-tennert.supabase:gotrue-kt")`
- `implementation("io.github.jan-tennert.supabase:postgrest-kt")`
- `implementation("io.github.jan-tennert.supabase:storage-kt")`

## How to Use

The Supabase client is automatically initialized in the `EsimaksiApplication` class. You can access it anywhere in your application using:

```kotlin
// Get the initialized client
val client = SupabaseClient.getClient()

// Use the client for database operations
val postgrest = client.postgrest
// Perform your operations...
```

For secure usage in production, consider using a more secure method to store your API keys rather than in string resources.

## Security Note

⚠️ **Important**: For production applications, do not store sensitive credentials like the Supabase Anon Key directly in the app code. Consider using more secure methods such as:
- Environment variables on your build system
- Encrypted storage
- A backend proxy for sensitive operations
package com.zawww.e_simaksi

import android.app.Application

class EsimaksiApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Supabase with your project URL and Anon Key
        // Replace these with your actual Supabase project URL and Anon Key
        val supabaseUrl = getString(R.string.supabase_url) // You can store these in string resources
        val supabaseAnonKey = getString(R.string.supabase_anon_key)
        
        // Alternatively, you can initialize with hardcoded values (not recommended for production)
        // SupabaseClient.initialize("your_supabase_project_url", "your_supabase_anon_key")
        
        // Or you can get these from BuildConfig if configured properly in gradle.properties
        // SupabaseClient.initialize(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY)
        
        // Initialize Supabase with the values
        //SupabaseClient.initialize(supabaseUrl, supabaseAnonKey)
    }
}
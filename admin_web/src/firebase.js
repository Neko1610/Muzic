// src/firebase.js

import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getDatabase } from "firebase/database";

// Firebase configuration (của bạn)
const firebaseConfig = {
  apiKey: "AIzaSyCV_g9FwFSF_9vcE7DxgDYOXjcV7sdY3O0",
  authDomain: "music-533b1.firebaseapp.com",
  databaseURL: "https://music-533b1-default-rtdb.firebaseio.com",
  projectId: "music-533b1",
  storageBucket: "music-533b1.firebasestorage.app",
  messagingSenderId: "359716177869",
  appId: "1:359716177869:web:126c4769194323cf08a684",
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// EXPORT CÁC THỨ ADMIN DASHBOARD CẦN
export const auth = getAuth(app);
export const db = getDatabase(app);

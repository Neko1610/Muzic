import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import AdminGuard from "./AdminGuard";
import AdminLayout from "./layouts/AdminLayout";

import AdminContent from "./pages/AdminContent";      // songs CRUD
import AdminUsers from "./pages/AdminUsers";
import AdminRoles from "./pages/AdminRoles";
import AdminCategories from "./pages/AdminCategories";
import AdminPlaylists from "./pages/AdminPlaylists";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />

        <Route
          path="/admin"
          element={
            <AdminGuard>
              <AdminLayout />
            </AdminGuard>
          }
        >
          <Route index element={<Navigate to="content" replace />} />

          <Route path="content" element={<AdminContent />} />
          <Route path="categories" element={<AdminCategories />} />
          <Route path="playlists" element={<AdminPlaylists />} />

          <Route path="users" element={<AdminUsers />} />
          <Route path="roles" element={<AdminRoles />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

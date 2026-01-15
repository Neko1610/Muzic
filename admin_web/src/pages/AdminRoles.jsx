import React from "react";

export default function AdminRoles() {
  return (
    <div>
      <div style={{ fontWeight: 950, opacity: 0.9 }}>Khung CRUD</div>

      <h2 style={{ margin: "10px 0 6px 0" }}>Phân quyền</h2>
      <p style={{ opacity: 0.85, marginTop: 0 }}>
        Trang này: đổi role user thành admin/user.
      </p>

      <ul style={{ opacity: 0.9, lineHeight: 1.7 }}>
        <li>
          Role nằm ở: <b>/users/{`{uid}`}/role</b>
        </li>
        <li>
          Nếu muốn đổi role cho <b>user khác</b> → làm trong trang <b>Users</b>
        </li>
      </ul>
    </div>
  );
}

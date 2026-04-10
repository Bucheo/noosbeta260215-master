import React, { useEffect, useState } from "react";

const AdminPage = () => {
  // 초기값을 빈 배열로 설정하여 .map() 에러 방지
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editUser, setEditUser] = useState(null); // 수정할 유저
  const [editForm, setEditForm] = useState({
    displayName: "",
    password: "",
    role: "",
  });
  const handleDelete = (userId, displayName) => {
    if (window.confirm(`정말 "${displayName}" 회원을 삭제하시겠습니까?`)) {
      fetch(`http://localhost:8080/api/admin/users/${userId}`, {
        method: "DELETE",
        credentials: "include",
      })
        .then((res) => {
          if (!res.ok) throw new Error("삭제 실패");
          // 삭제 후 목록에서 제거
          setUsers((prev) => prev.filter((u) => u.userId !== userId));
          alert("삭제되었습니다.");
        })
        .catch((err) => alert(err.message));
    }
  };

  const handleEditOpen = (user) => {
    setEditUser(user);
    setEditForm({
      displayName: user.displayName,
      password: "",
      role: user.role,
    });
  };

  const handleEditSave = () => {
    fetch(`http://localhost:8080/api/admin/users/${editUser.userId}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify(editForm),
    })
      .then((res) => {
        if (!res.ok) throw new Error("수정 실패");
        // 화면 즉시 반영
        setUsers((prev) =>
          prev.map((u) =>
            u.userId === editUser.userId
              ? { ...u, displayName: editForm.displayName, role: editForm.role }
              : u,
          ),
        );
        setEditUser(null);
        alert("수정되었습니다.");
      })
      .catch((err) => alert(err.message));
  };

  useEffect(() => {
    // 먼저 로그인 상태 확인
    fetch("http://localhost:8080/api/admin/me", {
      credentials: "include",
    })
      .then((res) => res.text())
      .then((text) => {
        return fetch("http://localhost:8080/api/admin/users", {
          credentials: "include",
        });
      })
      .then((res) => {
        if (!res) return;
        if (res.status === 403) throw new Error("관리자 권한이 없습니다.");
        return res.json();
      })
      .then((data) => {
        if (Array.isArray(data)) setUsers(data);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message);
        setLoading(false);
      });
  }, []);

  // 공통 스타일 정의
  const tableStyle = {
    padding: "12px",
    textAlign: "center",
    borderBottom: "1px solid #444",
  };
  const headerStyle = {
    ...tableStyle,
    backgroundColor: "#333",
    color: "#fff",
    fontWeight: "bold",
  };

  if (loading)
    return <div style={{ color: "white", padding: "20px" }}>로딩 중...</div>;
  if (error)
    return <div style={{ color: "red", padding: "20px" }}>에러: {error}</div>;

  return (
    <div
      style={{
        padding: "40px",
        backgroundColor: "#1a1a1a",
        minHeight: "100vh",
        color: "#fff",
      }}
    >
      <h2
        style={{
          marginBottom: "20px",
          borderBottom: "2px solid #444",
          paddingBottom: "10px",
        }}
      >
        🛡️ 관리자 페이지 - 회원 관리
      </h2>

      <table
        style={{
          width: "100%",
          borderCollapse: "collapse",
          backgroundColor: "#2d2d2d",
        }}
      >
        <thead>
          <tr>
            <th style={headerStyle}>번호</th>
            <th style={headerStyle}>아이디</th>
            <th style={headerStyle}>패스워드(Hash)</th>
            <th style={headerStyle}>이름</th>
            <th style={headerStyle}>권한</th>
            <th style={headerStyle}>생성일</th>
            <th style={headerStyle}>관리</th>
          </tr>
        </thead>
        <tbody>
          {users.length > 0 ? (
            users.map((user) => (
              <tr key={user.userId}>
                <td style={tableStyle}>{user.userId}</td>
                <td style={tableStyle}>{user.loginId}</td>
                <td style={tableStyle}>
                  <span
                    title={user.passwordHash}
                    style={{ cursor: "help", color: "#888" }}
                  >
                    {user.passwordHash?.substring(0, 10)}...
                  </span>
                </td>
                <td style={tableStyle}>{user.displayName}</td>
                <td style={tableStyle}>
                  <span
                    style={{
                      padding: "4px 8px",
                      borderRadius: "4px",
                      backgroundColor:
                        user.role === "ADMIN" ? "#d32f2f" : "#388e3c",
                      fontSize: "12px",
                    }}
                  >
                    {user.role}
                  </span>
                </td>
                <td style={tableStyle}>
                  {user.createdAt
                    ? new Date(user.createdAt).toLocaleDateString()
                    : "-"}
                </td>
                <td style={tableStyle}>
                  <button style={btnStyle} onClick={() => handleEditOpen(user)}>
                    수정
                  </button>
                  <button
                    style={{ ...btnStyle, backgroundColor: "#c62828" }}
                    onClick={() => handleDelete(user.userId, user.displayName)}
                  >
                    삭제
                  </button>
                </td>
              </tr>
            ))
          ) : (
            <tr>
              <td
                colSpan="7"
                style={{ padding: "30px", textAlign: "center", color: "#999" }}
              >
                표시할 회원 데이터가 없습니다.
              </td>
            </tr>
          )}
        </tbody>
      </table>
      {/* 수정 모달 */}
      {editUser && (
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            width: "100%",
            height: "100%",
            backgroundColor: "rgba(0,0,0,0.7)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 1000,
          }}
        >
          <div
            style={{
              backgroundColor: "#2d2d2d",
              padding: "30px",
              borderRadius: "8px",
              width: "400px",
              color: "#fff",
            }}
          >
            <h3
              style={{
                marginBottom: "20px",
                borderBottom: "1px solid #444",
                paddingBottom: "10px",
              }}
            >
              회원 수정 - {editUser.displayName}
            </h3>

            <div style={{ marginBottom: "15px" }}>
              <label
                style={{ display: "block", marginBottom: "5px", color: "#aaa" }}
              >
                이름
              </label>
              <input
                value={editForm.displayName}
                onChange={(e) =>
                  setEditForm({ ...editForm, displayName: e.target.value })
                }
                style={{
                  width: "100%",
                  padding: "8px",
                  backgroundColor: "#444",
                  border: "1px solid #666",
                  borderRadius: "4px",
                  color: "#fff",
                }}
              />
            </div>

            <div style={{ marginBottom: "15px" }}>
              <label
                style={{ display: "block", marginBottom: "5px", color: "#aaa" }}
              >
                새 비밀번호 (변경 안 하면 빈칸)
              </label>
              <input
                type="password"
                value={editForm.password}
                onChange={(e) =>
                  setEditForm({ ...editForm, password: e.target.value })
                }
                style={{
                  width: "100%",
                  padding: "8px",
                  backgroundColor: "#444",
                  border: "1px solid #666",
                  borderRadius: "4px",
                  color: "#fff",
                }}
              />
            </div>

            <div style={{ marginBottom: "25px" }}>
              <label
                style={{ display: "block", marginBottom: "5px", color: "#aaa" }}
              >
                권한
              </label>
              <select
                value={editForm.role}
                onChange={(e) =>
                  setEditForm({ ...editForm, role: e.target.value })
                }
                style={{
                  width: "100%",
                  padding: "8px",
                  backgroundColor: "#444",
                  border: "1px solid #666",
                  borderRadius: "4px",
                  color: "#fff",
                }}
              >
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>

            <div
              style={{
                display: "flex",
                justifyContent: "flex-end",
                gap: "10px",
              }}
            >
              <button
                onClick={() => setEditUser(null)}
                style={{
                  padding: "8px 20px",
                  backgroundColor: "#666",
                  color: "#fff",
                  border: "none",
                  borderRadius: "4px",
                  cursor: "pointer",
                }}
              >
                취소
              </button>
              <button
                onClick={handleEditSave}
                style={{
                  padding: "8px 20px",
                  backgroundColor: "#1976d2",
                  color: "#fff",
                  border: "none",
                  borderRadius: "4px",
                  cursor: "pointer",
                }}
              >
                저장
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

const btnStyle = {
  marginRight: "5px",
  padding: "5px 10px",
  backgroundColor: "#1976d2",
  color: "white",
  border: "none",
  borderRadius: "3px",
  cursor: "pointer",
  fontSize: "12px",
};

export default AdminPage;

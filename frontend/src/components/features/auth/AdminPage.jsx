import React, { useEffect, useState } from 'react';

const AdminPage = () => {
    const [users, setUsers] = useState([]);

    useEffect(() => {
        // 관리자용 유저 목록 API 호출
        fetch('http://localhost:8080/api/admin/users', { credentials: 'include' })
            .then(res => res.json())
            .then(data => setUsers(data))
            .catch(err => console.error("데이터 로드 실패:", err));
    }, []);

    return (
        <div style={{ padding: '20px' }}>
            <h2>관리자 페이지 - 회원 관리</h2>
            <table border="1" style={{ width: '100%', textAlign: 'left', borderCollapse: 'collapse' }}>
                <thead>
                    <tr style={{ backgroundColor: '#ffffffff', color: '#000' }}>
                        <th>ID</th>
                        <th>로그인 아이디</th>
                        <th>이름</th>
                        <th>권한</th>
                        <th>가입일</th>
                        <th>관리</th>
                    </tr>
                </thead>
                <tbody>
                    {users.map(user => (
                        <tr key={user.userId}>
                            <td>{user.userId}</td>
                            <td>{user.loginId}</td>
                            <td>{user.displayName}</td>
                            <td><strong>{user.role}</strong></td>
                            <td>{new Date(user.createdAt).toLocaleDateString()}</td>
                            <td>
                                <button onClick={() => alert('삭제 기능 구현 예정')}>삭제</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default AdminPage;
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import LoginPage from './pages/LoginPage';

function App() {
    return (
        <BrowserRouter>
            <Routes>
                {/* Tạm thời cài đặt đường dẫn mặc định (/) trỏ thẳng vào trang Đăng nhập */}
                <Route path="/" element={<LoginPage />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
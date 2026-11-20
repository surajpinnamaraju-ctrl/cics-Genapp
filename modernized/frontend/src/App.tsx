import { NavLink, Navigate, Route, Routes } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import Customers from './pages/Customers';
import Policies from './pages/Policies';

export default function App() {
  return (
    <div>
      <header className="app-header">
        <div className="brand">
          GENAPP
          <small>General Insurance Administration</small>
        </div>
        <nav className="nav">
          <NavLink to="/" end>Home</NavLink>
          <NavLink to="/customers">Customers</NavLink>
          <NavLink to="/policies/motor">Motor</NavLink>
          <NavLink to="/policies/endowment">Endowment</NavLink>
          <NavLink to="/policies/house">House</NavLink>
          <NavLink to="/policies/commercial">Commercial</NavLink>
        </nav>
      </header>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/customers" element={<Customers />} />
        <Route path="/policies/:type" element={<Policies />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </div>
  );
}

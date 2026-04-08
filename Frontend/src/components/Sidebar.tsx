import { useNavigate, useLocation } from "react-router-dom";

export default function Sidebar() {
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = (e: React.MouseEvent) => {
    e.preventDefault();
    localStorage.removeItem("user");
    navigate("/");
  };

  const navItems = [
    { label: "Dashboard", icon: "dashboard", path: "/dashboard" },
    { label: "Warehouse Inventory", icon: "warehouse", path: "/warehouse-inventory" },
    { label: "Products", icon: "inventory_2", path: "/product" },
    { label: "Branch", icon: "storefront", path: "/branches" },
    { label: "Suppliers", icon: "local_shipping", path: "#" },
    { label: "Billing", icon: "payments", path: "#" },
    { label: "Orders", icon: "receipt_long", path: "/warehouse-orders" },
    { label: "Reports", icon: "analytics", path: "#" },
  ];

  const secondaryItems = [
    { label: "Settings", icon: "settings", path: "#" },
    { label: "Help", icon: "help", path: "#" },
  ];

  return (
    <aside className="bg-[#0c0f10] h-screen w-56 fixed left-0 top-0 flex flex-col py-8 z-50">
      <div className="px-8 mb-10">
        <h1
          className="text-2xl font-black text-[#C6FF3D] tracking-tighter italic cursor-pointer"
          onClick={() => navigate("/dashboard")}
        >
          Ventorie
        </h1>
      </div>

      <nav className="flex-1 space-y-1">
        {navItems.map((item) => {
          const isActive = location.pathname === item.path;
          return (
            <div
              key={item.label}
              onClick={() => item.path !== "#" && navigate(item.path)}
              className={`flex items-center px-8 py-3 cursor-pointer font-['Inter'] font-medium tracking-tight transition-all duration-200 ${isActive
                  ? "text-[#C6FF3D] border-l-4 border-[#C6FF3D] bg-white/5"
                  : "text-gray-500 hover:text-gray-300 hover:bg-white/5"
                }`}
            >
              <span className={`material-symbols-outlined mr-3 ${isActive ? "text-[#C6FF3D]" : ""}`}>
                {item.icon}
              </span>
              <span>{item.label}</span>
            </div>
          );
        })}

        <div className="mt-8 pt-8 border-t border-white/5">
          {secondaryItems.map((item) => (
            <div
              key={item.label}
              className="flex items-center px-8 py-3 text-gray-500 hover:text-gray-300 hover:bg-white/5 font-['Inter'] font-medium tracking-tight transition-all duration-200 cursor-pointer"
            >
              <span className="material-symbols-outlined mr-3">{item.icon}</span>
              <span>{item.label}</span>
            </div>
          ))}

          <div
            onClick={handleLogout}
            className="flex items-center px-8 py-3 text-gray-500 hover:text-gray-300 hover:bg-white/5 font-['Inter'] font-medium tracking-tight transition-all duration-200 cursor-pointer"
          >
            <span className="material-symbols-outlined mr-3">logout</span>
            <span>Logout</span>
          </div>
        </div>
      </nav>
    </aside>
  );
}

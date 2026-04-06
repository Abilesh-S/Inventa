import React from "react";

interface HeaderProps {
    title: string;
    subtitle: string;
    searchPlaceholder?: string;
    icon?: string;
    children?: React.ReactNode;
    searchValue?: string;
    onSearchChange?: (val: string) => void;
}

const Header: React.FC<HeaderProps> = ({ 
    title, 
    subtitle, 
    searchPlaceholder = "Search...", 
    icon = "folder_shared", 
    children,
    searchValue,
    onSearchChange
}) => {
    // Get user from localStorage
    const userStr = localStorage.getItem("user");
    const user = userStr ? JSON.parse(userStr) : null;
    const userName = user?.name || "Guest User";
    
    // Dynamic Role Mapping
    const getDisplayRole = (role: string) => {
        if (!role) return "Access Granted";
        const r = role.toUpperCase();
        if (r === "OWNER") return "Corporate Owner";
        if (r === "MANAGER") return "Branch Manager";
        if (r === "STAFF") return "Operations Staff";
        return r.charAt(0) + r.slice(1).toLowerCase();
    };

    const userRole = getDisplayRole(user?.role);
    const businessContext = user?.business?.name || user?.branch?.branchName || "Global Hub";

    return (
        <header className="flex justify-between items-center px-10 py-6 w-full bg-[#f5f6f8]/90 backdrop-blur-xl sticky top-0 z-40 border-b border-black/5 shadow-sm">
            <div className="flex items-center gap-10">
                <div className="flex items-center gap-4">
                    <span className="material-symbols-outlined text-[#abadaf] text-lg">{icon}</span>
                    <div className="flex flex-col">
                        <span className="text-[10px] font-black text-[#abadaf] uppercase tracking-[0.2em] leading-none mb-1">{subtitle} • {businessContext}</span>
                        <h2 className="text-2xl font-black tracking-tight text-[#0c0f10] uppercase">{title}</h2>
                    </div>
                </div>
                <div className="hidden md:flex items-center bg-[#eff1f3] px-4 py-2.5 rounded-full border border-black/5 w-72 focus-within:ring-2 focus-within:ring-[#c5fe3c] transition-all">
                    <span className="material-symbols-outlined text-[#757779] text-sm mr-2">search</span>
                    <input 
                        className="bg-transparent border-none focus:ring-0 text-xs w-full outline-none font-medium" 
                        placeholder={searchPlaceholder} 
                        type="text"
                        value={searchValue || ""}
                        onChange={(e) => onSearchChange?.(e.target.value)}
                    />
                </div>
            </div>
            
            <div className="flex items-center gap-6">
                {children}
                <div className="flex items-center gap-3 cursor-pointer hover:opacity-80 transition-opacity">
                    <div className="text-right hidden sm:block">
                        <p className="text-sm font-bold tracking-tight text-[#0c0f10]">{userName}</p>
                        <p className="text-[10px] text-[#abadaf] uppercase tracking-widest leading-none">{userRole}</p>
                    </div>
                    <div className="w-10 h-10 rounded-xl overflow-hidden bg-[#c5fe3c] flex items-center justify-center ring-2 ring-white shadow-md">
                        <div className="w-full h-full flex items-center justify-center text-black font-black text-lg bg-[#c5fe3c]">
                            {userName.charAt(0).toUpperCase()}
                        </div>
                    </div>
                </div>
            </div>
        </header>
    );
};

export default Header;

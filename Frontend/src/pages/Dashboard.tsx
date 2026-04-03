
import { Link, useNavigate } from "react-router-dom";

export default function Dashboard() {
  const navigate = useNavigate();

  const handleLogout = (e: React.MouseEvent) => {
    e.preventDefault();
    localStorage.removeItem("user");
    navigate("/");
  };

  return (
    <div className="bg-background text-on-surface min-h-screen">
      {/* NavigationDrawer */}
      <aside className="bg-[#0c0f10] h-screen w-64 fixed left-0 top-0 flex flex-col py-8 z-50">
        <div className="px-8 mb-10">
          <h1 className="text-2xl font-black text-[#C6FF3D] tracking-tighter italic">Ventorie</h1>
        </div>
        <nav className="flex-1 space-y-1">
          <Link
            className="flex items-center px-8 py-3 text-[#C6FF3D] border-l-4 border-[#C6FF3D] bg-white/5 font-['Inter'] font-medium tracking-tight transition-all duration-200"
            to="/dashboard"
          >
            <span className="material-symbols-outlined mr-3">dashboard</span>
            <span>Dashboard</span>
          </Link>
          <a
            className="flex items-center px-8 py-3 text-gray-500 hover:text-gray-300 hover:bg-white/5 font-['Inter'] font-medium tracking-tight transition-all duration-200"
            href="#"
          >
            <span className="material-symbols-outlined mr-3">storefront</span>
            <span>Stores</span>
          </a>
          <a
            className="flex items-center px-8 py-3 text-gray-500 hover:text-gray-300 hover:bg-white/5 font-['Inter'] font-medium tracking-tight transition-all duration-200"
            href="#"
          >
            <span className="material-symbols-outlined mr-3">inventory_2</span>
            <span>Products</span>
          </a>
          <a
            className="flex items-center px-8 py-3 text-gray-500 hover:text-gray-300 hover:bg-white/5 font-['Inter'] font-medium tracking-tight transition-all duration-200"
            href="#"
          >
            <span className="material-symbols-outlined mr-3">category</span>
            <span>Category</span>
          </a>
          <a
            className="flex items-center px-8 py-3 text-gray-500 hover:text-gray-300 hover:bg-white/5 font-['Inter'] font-medium tracking-tight transition-all duration-200"
            href="#"
          >
            <span className="material-symbols-outlined mr-3">local_shipping</span>
            <span>Suppliers</span>
          </a>
          <a
            className="flex items-center px-8 py-3 text-gray-500 hover:text-gray-300 hover:bg-white/5 font-['Inter'] font-medium tracking-tight transition-all duration-200"
            href="#"
          >
            <span className="material-symbols-outlined mr-3">payments</span>
            <span>Billing</span>
          </a>
          <a
            className="flex items-center px-8 py-3 text-gray-500 hover:text-gray-300 hover:bg-white/5 font-['Inter'] font-medium tracking-tight transition-all duration-200"
            href="#"
          >
            <span className="material-symbols-outlined mr-3">receipt_long</span>
            <span>Orders</span>
          </a>
          <a
            className="flex items-center px-8 py-3 text-gray-500 hover:text-gray-300 hover:bg-white/5 font-['Inter'] font-medium tracking-tight transition-all duration-200"
            href="#"
          >
            <span className="material-symbols-outlined mr-3">local_post_office</span>
            <span>Delivery</span>
          </a>
          <a
            className="flex items-center px-8 py-3 text-gray-500 hover:text-gray-300 hover:bg-white/5 font-['Inter'] font-medium tracking-tight transition-all duration-200"
            href="#"
          >
            <span className="material-symbols-outlined mr-3">analytics</span>
            <span>Reports</span>
          </a>
          <div className="mt-8 pt-8 border-t border-white/5">
            <a
              className="flex items-center px-8 py-3 text-gray-500 hover:text-gray-300 hover:bg-white/5 font-['Inter'] font-medium tracking-tight transition-all duration-200"
              href="#"
            >
              <span className="material-symbols-outlined mr-3">settings</span>
              <span>Settings</span>
            </a>
            <a
              className="flex items-center px-8 py-3 text-gray-500 hover:text-gray-300 hover:bg-white/5 font-['Inter'] font-medium tracking-tight transition-all duration-200"
              href="#"
            >
              <span className="material-symbols-outlined mr-3">help</span>
              <span>Help</span>
            </a>
            <a
              onClick={handleLogout}
              className="flex items-center px-8 py-3 text-gray-500 hover:text-gray-300 hover:bg-white/5 font-['Inter'] font-medium tracking-tight transition-all duration-200 cursor-pointer"
            >
              <span className="material-symbols-outlined mr-3">logout</span>
              <span>Logout</span>
            </a>
          </div>
        </nav>
      </aside>

      {/* Main Content Area */}
      <main className="ml-64 min-h-screen bg-background">
        {/* TopAppBar */}
        <header className="w-full h-16 sticky top-0 z-40 bg-[#f5f6f8] dark:bg-[#0c0f10] flex items-center justify-between px-8">
          <div className="flex items-center bg-surface-container-low rounded-xl px-4 py-2 w-96 focus-within:ring-2 focus-within:ring-[#C6FF3D] transition-all">
            <span className="material-symbols-outlined text-on-surface-variant mr-2">search</span>
            <input
              className="bg-transparent border-none focus:ring-0 text-sm w-full font-medium focus:outline-none"
              placeholder="Search for products, orders..."
              type="text"
            />
          </div>
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-3 cursor-pointer hover:opacity-80 transition-opacity">
              <div className="text-right">
                <p className="text-sm font-bold tracking-tight text-on-surface">Super Admin</p>
                <p className="text-[10px] text-on-surface-variant uppercase tracking-widest">Enterprise Access</p>
              </div>
              <div className="w-10 h-10 rounded-xl overflow-hidden bg-primary-container flex items-center justify-center">
                <img
                  alt="Admin Profile"
                  className="w-full h-full object-cover"
                  src="https://lh3.googleusercontent.com/aida-public/AB6AXuD2J-jM2Odjyq4U3N_4fQY5p3Io_eECDpOwin5H1hkPkbVZWnaPs--PoV52QG4lY83vTBTeSGALG2MN3Or-pwqGGHe59zGvfvYlNpiPvlNOrxJiNUJfkT_vPhbQmfamYGHgEP4MccVV1IpkDV3WgvRSrRUkZ-C_D5VL0laLp_H82GVNHkrAdJoM22BUcXE4s6gINHw71OBH2VdJhKiODYv8iOhLsIba6tUt1J3bA67FoGtGEH4O9OL20ylEi-pp3nGGeqEVz7nUJ7w"
                />
              </div>
            </div>
          </div>
        </header>

        {/* Dashboard Content */}
        <div className="p-8 max-w-[1600px] mx-auto space-y-8">
          {/* Stats Bento Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {/* Card 1 */}
            <div className="bg-surface-container-lowest p-6 rounded-xl hover:-translate-y-1 hover:shadow-[0px_24px_48px_rgba(44,47,49,0.06)] transition-all duration-300">
              <div className="flex justify-between items-start mb-4">
                <div className="p-2 bg-surface-container-low rounded-lg">
                  <span className="material-symbols-outlined text-primary">inventory_2</span>
                </div>
                <div className="flex items-center gap-1 bg-primary-container/20 px-2 py-1 rounded-full">
                  <span
                    className="material-symbols-outlined text-[12px] text-primary"
                    style={{ fontVariationSettings: "'FILL' 1" }}
                  >
                    trending_up
                  </span>
                  <span className="text-[10px] font-bold text-on-primary-container">+12%</span>
                </div>
              </div>
              <p className="text-on-surface-variant text-sm font-medium">Total Products</p>
              <h2 className="text-3xl font-black text-on-surface tracking-tighter mt-1">24,512</h2>
            </div>
            
            {/* Card 2 */}
            <div className="bg-surface-container-lowest p-6 rounded-xl hover:-translate-y-1 hover:shadow-[0px_24px_48px_rgba(44,47,49,0.06)] transition-all duration-300">
              <div className="flex justify-between items-start mb-4">
                <div className="p-2 bg-surface-container-low rounded-lg">
                  <span className="material-symbols-outlined text-primary">check_circle</span>
                </div>
                <div className="flex items-center gap-1 bg-primary-container/20 px-2 py-1 rounded-full">
                  <span
                    className="material-symbols-outlined text-[12px] text-primary"
                    style={{ fontVariationSettings: "'FILL' 1" }}
                  >
                    trending_up
                  </span>
                  <span className="text-[10px] font-bold text-on-primary-container">+5%</span>
                </div>
              </div>
              <p className="text-on-surface-variant text-sm font-medium">Available Stock</p>
              <h2 className="text-3xl font-black text-on-surface tracking-tighter mt-1">18,209</h2>
            </div>
            
            {/* Card 3 - Replaced with Expired Product Count */}
            <div className="bg-surface-container-lowest p-6 rounded-xl hover:-translate-y-1 hover:shadow-[0px_24px_48px_rgba(44,47,49,0.06)] transition-all duration-300">
              <div className="flex justify-between items-start mb-4">
                <div className="p-2 bg-surface-container-low rounded-lg">
                  <span className="material-symbols-outlined text-error">warning</span>
                </div>
                <div className="flex items-center gap-1 bg-error-container/20 px-2 py-1 rounded-full">
                  <span
                    className="material-symbols-outlined text-[12px] text-error"
                    style={{ fontVariationSettings: "'FILL' 1" }}
                  >
                    trending_up
                  </span>
                  <span className="text-[10px] font-bold text-on-error-container">+4.2%</span>
                </div>
              </div>
              <p className="text-on-surface-variant text-sm font-medium">Expired Products</p>
              <h2 className="text-3xl font-black text-on-surface tracking-tighter mt-1">1,240</h2>
            </div>
            
            {/* Card 4 - Replaced with Monthly Food Waste */}
            <div className="bg-surface-container-lowest p-6 rounded-xl hover:-translate-y-1 hover:shadow-[0px_24px_48px_rgba(44,47,49,0.06)] transition-all duration-300">
              <div className="flex justify-between items-start mb-4">
                <div className="p-2 bg-surface-container-low rounded-lg">
                  <span className="material-symbols-outlined text-error-dim">delete_sweep</span>
                </div>
                <div className="flex items-center gap-1 bg-error-container/20 px-2 py-1 rounded-full">
                  <span
                    className="material-symbols-outlined text-[12px] text-error-dim"
                    style={{ fontVariationSettings: "'FILL' 1" }}
                  >
                    trending_down
                  </span>
                  <span className="text-[10px] font-bold text-on-error-container">-2.1%</span>
                </div>
              </div>
              <p className="text-on-surface-variant text-sm font-medium">Monthly Food Waste</p>
              <h2 className="text-3xl font-black text-on-surface tracking-tighter mt-1">45.2 kg</h2>
            </div>
          </div>

          {/* Charts & Analytics */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Order Summary Line Chart Mockup */}
            <div className="lg:col-span-2 bg-surface-container-lowest p-8 rounded-xl relative overflow-hidden group">
              <div className="flex justify-between items-end mb-8">
                <div>
                  <h3 className="text-xl font-bold tracking-tight text-on-surface">Order Summary</h3>
                  <p className="text-on-surface-variant text-sm">Monthly throughput overview</p>
                </div>
                <div className="flex gap-2">
                  <span className="px-3 py-1 bg-surface-container-low text-xs font-bold rounded-full cursor-pointer hover:bg-primary-container transition-colors">W</span>
                  <span className="px-3 py-1 bg-primary-container text-xs font-bold rounded-full cursor-pointer">M</span>
                  <span className="px-3 py-1 bg-surface-container-low text-xs font-bold rounded-full cursor-pointer hover:bg-primary-container transition-colors">Y</span>
                </div>
              </div>
              {/* Chart Visualization (SVG CSS) */}
              <div className="h-64 w-full mt-4 flex items-end justify-between px-2 gap-4">
                <div className="w-full bg-surface-container-low rounded-t-xl h-2/3 group-hover:h-3/4 transition-all duration-700 relative">
                  <div className="absolute inset-x-0 bottom-0 neon-gradient h-1/2 opacity-20 rounded-t-xl" />
                </div>
                <div className="w-full bg-surface-container-low rounded-t-xl h-1/2 group-hover:h-2/3 transition-all duration-700 relative">
                  <div className="absolute inset-x-0 bottom-0 neon-gradient h-1/2 opacity-40 rounded-t-xl" />
                </div>
                <div className="w-full bg-surface-container-low rounded-t-xl h-3/4 group-hover:h-5/6 transition-all duration-700 relative">
                  <div className="absolute inset-x-0 bottom-0 neon-gradient h-1/2 opacity-60 rounded-t-xl" />
                </div>
                <div className="w-full bg-surface-container-low rounded-t-xl h-2/5 group-hover:h-1/2 transition-all duration-700 relative">
                  <div className="absolute inset-x-0 bottom-0 neon-gradient h-1/2 opacity-30 rounded-t-xl" />
                </div>
                <div className="w-full bg-surface-container-low rounded-t-xl h-4/5 group-hover:h-full transition-all duration-700 relative">
                  <div className="absolute inset-x-0 bottom-0 neon-gradient h-1/2 opacity-80 rounded-t-xl" />
                </div>
                <div className="w-full bg-surface-container-low rounded-t-xl h-1/2 group-hover:h-3/5 transition-all duration-700 relative">
                  <div className="absolute inset-x-0 bottom-0 neon-gradient h-1/2 opacity-50 rounded-t-xl" />
                </div>
                <div className="w-full bg-surface-container-low rounded-t-xl h-2/3 group-hover:h-3/4 transition-all duration-700 relative">
                  <div className="absolute inset-x-0 bottom-0 neon-gradient h-1/2 opacity-20 rounded-t-xl" />
                </div>
              </div>
              <div className="flex justify-between mt-4 px-2 text-[10px] font-bold text-on-surface-variant tracking-widest uppercase">
                <span>Mon</span><span>Tue</span><span>Wed</span><span>Thu</span><span>Fri</span><span>Sat</span><span>Sun</span>
              </div>
            </div>
            
            {/* Profit by Category Donut Chart Mockup */}
            <div className="bg-surface-container-lowest p-8 rounded-xl flex flex-col items-center justify-center">
              <h3 className="text-xl font-bold tracking-tight text-on-surface mb-2 self-start">Profit by Category</h3>
              <p className="text-on-surface-variant text-sm mb-8 self-start">Total: $1.2M this year</p>
              <div className="relative w-48 h-48 flex items-center justify-center">
                <svg className="w-full h-full transform -rotate-90">
                  <circle className="text-surface-container-low" cx="96" cy="96" fill="transparent" r="80" stroke="currentColor" strokeWidth="20" />
                  <circle className="text-primary" cx="96" cy="96" fill="transparent" r="80" stroke="currentColor" strokeDasharray="502" strokeDashoffset="150" strokeWidth="20" />
                  <circle className="text-on-surface" cx="96" cy="96" fill="transparent" r="80" stroke="currentColor" strokeDasharray="502" strokeDashoffset="400" strokeWidth="20" />
                </svg>
                <div className="absolute flex flex-col items-center">
                  <span className="text-2xl font-black tracking-tighter">72%</span>
                  <span className="text-[10px] font-bold text-on-surface-variant uppercase tracking-widest">Growth</span>
                </div>
              </div>
              <div className="w-full mt-8 space-y-3">
                <div className="flex justify-between items-center">
                  <div className="flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-primary" />
                    <span className="text-xs font-medium">Electronics</span>
                  </div>
                  <span className="text-xs font-bold">45%</span>
                </div>
                <div className="flex justify-between items-center">
                  <div className="flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-on-surface" />
                    <span className="text-xs font-medium">Apparel</span>
                  </div>
                  <span className="text-xs font-bold">30%</span>
                </div>
                <div className="flex justify-between items-center">
                  <div className="flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-surface-dim" />
                    <span className="text-xs font-medium">Home Goods</span>
                  </div>
                  <span className="text-xs font-bold">25%</span>
                </div>
              </div>
            </div>
          </div>

          {/* Bottom Section: Stock Levels & Activity */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Stock Levels Progress Bars */}
            <div className="bg-surface-container-lowest p-8 rounded-xl">
              <div className="flex justify-between items-center mb-6">
                <h3 className="text-xl font-bold tracking-tight text-on-surface">Critical Stock Levels</h3>
                <button className="text-xs font-bold text-primary hover:underline">View All Inventory</button>
              </div>
              <div className="space-y-6">
                <div className="group">
                  <div className="flex justify-between text-sm mb-2">
                    <span className="font-medium text-on-surface">Smart Watch Gen-4</span>
                    <span className="font-bold text-error">12 left</span>
                  </div>
                  <div className="w-full bg-surface-container-low h-2 rounded-full overflow-hidden">
                    <div className="bg-error h-full transition-all duration-1000" style={{ width: "12%" }} />
                  </div>
                </div>
                <div className="group">
                  <div className="flex justify-between text-sm mb-2">
                    <span className="font-medium text-on-surface">Noise Cancelling Headphones</span>
                    <span className="font-bold text-on-surface-variant">45 left</span>
                  </div>
                  <div className="w-full bg-surface-container-low h-2 rounded-full overflow-hidden">
                    <div className="bg-primary-container h-full transition-all duration-1000" style={{ width: "45%" }} />
                  </div>
                </div>
                <div className="group">
                  <div className="flex justify-between text-sm mb-2">
                    <span className="font-medium text-on-surface">4K Ultra Monitor 32"</span>
                    <span className="font-bold text-on-surface-variant">28 left</span>
                  </div>
                  <div className="w-full bg-surface-container-low h-2 rounded-full overflow-hidden">
                    <div className="bg-primary h-full transition-all duration-1000" style={{ width: "28%" }} />
                  </div>
                </div>
                <div className="group">
                  <div className="flex justify-between text-sm mb-2">
                    <span className="font-medium text-on-surface">Mechanical Wireless Keyboard</span>
                    <span className="font-bold text-error">8 left</span>
                  </div>
                  <div className="w-full bg-surface-container-low h-2 rounded-full overflow-hidden">
                    <div className="bg-error-container h-full transition-all duration-1000" style={{ width: "8%" }} />
                  </div>
                </div>
              </div>
            </div>

            {/* Recent Activity */}
            <div className="bg-surface-container-lowest p-8 rounded-xl">
              <h3 className="text-xl font-bold tracking-tight text-on-surface mb-6">Live Operations</h3>
              <div className="space-y-6">
                <div className="flex items-center gap-4">
                  <div className="w-10 h-10 rounded-full overflow-hidden bg-surface-container">
                    <img
                      alt="User"
                      className="w-full h-full object-cover"
                      src="https://lh3.googleusercontent.com/aida-public/AB6AXuBPFlZ2cFSJr9lNgkXxLPfxTr6SXXCct98LvYmKqtQHkb7vZrL12-GgTXkasH9RJl4roBSfH56VXNVOQ7zwAbhEy18mYkOIrEl3xeV9KHq1tpPonpjnwe6mYSHu4OX5rAH3JGj_CEXlC1GlZEcFXDLZFDkhRVhNCbWY-dQRS6OGjACeazTVCCkqN8q8jFndy2KQ4_Zgvt1UrC3qsF_Cy2OlmZbTijunbmsfnPicLO-S-nDj0AbEqKJeTfPFWKzJlHP3O8CjBueyYac"
                    />
                  </div>
                  <div className="flex-1">
                    <p className="text-sm font-medium text-on-surface"><span className="font-bold">Sarah Jenkins</span> authorized order #8821</p>
                    <p className="text-[10px] text-on-surface-variant font-bold tracking-widest uppercase">2 mins ago</p>
                  </div>
                  <div className="px-2 py-1 bg-surface-container-low rounded-lg">
                    <span className="material-symbols-outlined text-sm">chevron_right</span>
                  </div>
                </div>
                <div className="flex items-center gap-4">
                  <div className="w-10 h-10 rounded-full overflow-hidden bg-surface-container">
                    <img
                      alt="User"
                      className="w-full h-full object-cover"
                      src="https://lh3.googleusercontent.com/aida-public/AB6AXuCpYeWI5Ke6stf2b9OzLonlR76lJ610vFbFEbY_XD2Lv_Rv2gmZaRNA0EsB94-l9VkJjQhPQ_G1miGpHtNEGWBQ3qJ9Zrcxy_EY4asVV6lA6Ih-ZD6mq4FwRpuXh6Lm13hgN5kfppoUyWX5DBReWUyLrE_yjjCR6Q-DfyU4XW47vf9j5is-LZRAKK78lwFVRJICKpNyep_bI9I6Y17i0328dBeZUQMrzhXYTfUO-hJX4qOE2GASR-A_q0D_9Lqe6iSz291iMhBtXKA"
                    />
                  </div>
                  <div className="flex-1">
                    <p className="text-sm font-medium text-on-surface"><span className="font-bold">Marco Ross</span> updated stock for 'Pro Laptops'</p>
                    <p className="text-[10px] text-on-surface-variant font-bold tracking-widest uppercase">15 mins ago</p>
                  </div>
                  <div className="px-2 py-1 bg-surface-container-low rounded-lg">
                    <span className="material-symbols-outlined text-sm">chevron_right</span>
                  </div>
                </div>
                <div className="flex items-center gap-4">
                  <div className="w-10 h-10 rounded-full overflow-hidden bg-surface-container">
                    <img
                      alt="User"
                      className="w-full h-full object-cover"
                      src="https://lh3.googleusercontent.com/aida-public/AB6AXuC5EKJLBcARotXigNoZVcXA_F3Zxnlw5trbAaapaTCBRNPLqV-hZ8XjdQbM1XdWyLccJAFxj-HIda9LS1v9OfaIzrks-C5m8etMuOQbAkUUELC6R3aMoZoCIfQ0j22pSAfzDLBpsy3jB9LuejQcco80twPVmTtPThQYcbR3h-_497WH3-Tg3atGpXjluWLSd5AWCJc3hhJ8o3mQ9J_Uy9ZiS2OgkZEtXAD6Ew0NCZhxPhP1uQX9Ja2DznhA1C96F305FR2rAZuugmY"
                    />
                  </div>
                  <div className="flex-1">
                    <p className="text-sm font-medium text-on-surface"><span className="font-bold">Lina Vo</span> flagged 'Out of Stock' alert</p>
                    <p className="text-[10px] text-on-surface-variant font-bold tracking-widest uppercase">42 mins ago</p>
                  </div>
                  <div className="px-2 py-1 bg-surface-container-low rounded-lg">
                    <span className="material-symbols-outlined text-sm">chevron_right</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>

      {/* FAB for Dashboard context */}
      <div className="fixed bottom-8 right-8 z-50">
        <button className="neon-gradient-btn w-14 h-14 rounded-full flex items-center justify-center shadow-xl hover:scale-110 transition-transform active:scale-95 group">
          <span className="material-symbols-outlined text-[#364b00] group-hover:rotate-90 transition-transform duration-300">add</span>
        </button>
      </div>
    </div>
  );
}

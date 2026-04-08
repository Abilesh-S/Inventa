import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Header from "../components/Header";

const API_BASE = "http://localhost:8080/Inventa/api";
const getAuthHeader = (user: any) => user?.token ? `Bearer ${user.token}` : `Basic ${user.auth}`;

interface DashboardData {
  warehouseName: string;
  totalProducts: number;
  warehouseStock: number;
  /** Sum of stock units across all branches (matches warehouse inventory KPI). */
  totalBranchInventoryUnits: number;
  inventoryCount: number;
  outOfStockCount: number;
  lowStockCount: number;
  expiringCount: number;
  totalBranches: number;
  foodWastage: number;
  totalProfit: number;
  profitGrowth: number;
  overallPercentage: number;
  recentActivity: any[];
  stockLevels: any[];
  orderSummary: any[];
  profitByCategory: any[];
}

export default function Dashboard() {
  const navigate = useNavigate();
  const [data, setData] = useState<DashboardData | null>(null);
  const [pendingRequests, setPendingRequests] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showNotifications, setShowNotifications] = useState(false);

  useEffect(() => {
    const userStr = localStorage.getItem("user");
    if (!userStr) {
      navigate("/");
      return;
    }

    const user = JSON.parse(userStr);
    if (!user.auth) {
      navigate("/");
      return;
    }

    const fetchStats = async () => {
      try {
        const res = await fetch(`${API_BASE}/dashboard/stats`, {
          headers: {
            'Authorization': getAuthHeader(user)
          }
        });
        if (res.ok) {
          const stats = await res.json();
          if (stats.error) {
            console.error("Backend Stats Error:", stats.error, stats.type);
          } else {
            setData(stats);
          }
        } else {
          console.error("Dashboard HTTP Error:", res.status);
        }
      } catch (err) {
        console.error("Failed to fetch dashboard stats", err);
      } finally {
        setLoading(false);
      }
    };

    const fetchPendingRequests = async () => {
      try {
        const res = await fetch(`${API_BASE}/stock-requests/pending`, {
          headers: { 'Authorization': getAuthHeader(user) }
        });
        if (res.ok) setPendingRequests(await res.json());
      } catch (err) {
        console.error("Failed to fetch pending requests", err);
      }
    };

    fetchStats();
    fetchPendingRequests();

    // Auto-refresh notifications every 30 seconds
    const interval = setInterval(fetchPendingRequests, 30000);
    return () => clearInterval(interval);
  }, [navigate]);

  const handleApproveRequest = async (id: number) => {
    const userStr = localStorage.getItem("user");
    if (!userStr) return;
    const user = JSON.parse(userStr);

    try {
      const res = await fetch(`${API_BASE}/stock-requests/${id}/approve`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": getAuthHeader(user)
        },
        body: JSON.stringify({ remark: "Approved by Owner" })
      });
      if (res.ok) {
        setPendingRequests(prev => prev.filter(r => r.id !== id));
      } else {
        const errData = await res.json().catch(() => ({}));
        alert(errData.error || "Failed to approve request. Stock might be insufficient.");
      }
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) {
    return (
      <div className="bg-background text-on-surface min-h-screen flex items-center justify-center">
        <p className="text-xl font-bold animate-pulse">Synchronizing Workspace...</p>
      </div>
    );
  }

  return (
    <div className="bg-background text-on-surface min-h-screen flex">
      <Sidebar />

      {/* Main Content Area */}
      <main className="ml-56 flex-1 min-h-screen bg-background">
        <Header
          title="Dashboard"
          subtitle="Operational Intelligence"
          searchPlaceholder="Search resources..."
          icon="dashboard"
        >
          <div className="flex items-center gap-6">
            {/* Notification Hub */}
            <div className="relative">
              <button
                onClick={() => setShowNotifications(!showNotifications)}
                className="relative p-2.5 bg-white border border-[#abadaf]/10 rounded-xl hover:bg-[#eff1f3] transition-colors"
              >
                <span className="material-symbols-outlined text-[#0c0f10] text-[22px]">notifications</span>
                {pendingRequests.length > 0 && (
                  <span className="absolute -top-1 -right-1 h-5 w-5 bg-error text-white text-[10px] font-black flex items-center justify-center rounded-full border-2 border-white animate-pulse">
                    {pendingRequests.length}
                  </span>
                )}
              </button>

              {/* Notification Popover */}
              {showNotifications && (
                <div className="absolute right-0 mt-4 w-[320px] bg-white rounded-2xl shadow-2xl border border-black/5 z-[50] overflow-hidden">
                  <div className="p-4 bg-[#f5f6f8] border-b border-black/5 flex justify-between items-center">
                    <p className="text-[10px] font-black uppercase tracking-[0.2em] text-[#0c0f10]">Pending Actions</p>
                    <span className="text-[10px] font-bold text-error">{pendingRequests.length} Requests</span>
                  </div>
                  <div className="max-h-[350px] overflow-y-auto no-scrollbar">
                    {pendingRequests.length === 0 ? (
                      <div className="p-10 text-center">
                        <span className="material-symbols-outlined text-[40px] text-[#abadaf] opacity-20 block mb-2">check_circle</span>
                        <p className="text-xs font-bold text-[#abadaf]">Logistics clear</p>
                      </div>
                    ) : (
                      pendingRequests.map((req) => (
                        <div key={req.id} className="p-4 border-b border-black/5 hover:bg-[#eff1f3]/50 transition-colors">
                          <div className="flex justify-between items-start mb-1">
                            <p className="text-sm font-black text-[#0c0f10] uppercase tracking-tighter">{req.ingredientName}</p>
                            <span className="text-[10px] font-black text-blue-600 bg-blue-50 px-2 py-0.5 rounded-full">New</span>
                          </div>
                          <p className="text-[10px] font-bold text-[#595c5e]">
                            Branch: <span className="text-[#0c0f10]">#{req.branchId}</span> •
                            Qty: <span className="text-[#0c0f10]">{req.requiredQuantity} {req.unit}</span>
                          </p>
                          <div className="mt-3">
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                handleApproveRequest(req.id);
                              }}
                              className="w-full py-1.5 bg-black text-[#c5fe3c] text-[9px] font-black uppercase tracking-widest rounded-lg hover:scale-105 transition-all shadow-md flex items-center justify-center gap-1"
                            >
                              <span className="material-symbols-outlined text-xs">local_shipping</span>
                              Dispatch now
                            </button>
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              )}
            </div>

            <div className="h-10 w-px bg-black/5 hidden md:block"></div>
          </div>
        </Header>

        {/* Dashboard Content */}
        <div className="p-8 space-y-8">
          {/* Stats Bento Grid - 4x2 Symmetric Layout */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {/* Card 1 - Branches */}
            <div className="bg-surface-container-lowest p-6 rounded-xl hover:-translate-y-1 hover:shadow-[0px_24px_48px_rgba(44,47,49,0.06)] transition-all duration-300">
              <div className="flex justify-between items-start mb-4">
                <div className="p-2 bg-surface-container-low rounded-lg">
                  <span className="material-symbols-outlined text-primary">storefront</span>
                </div>
              </div>
              <p className="text-on-surface-variant text-sm font-medium">Active Branches</p>
              <h2 className="text-3xl font-black text-on-surface tracking-tighter mt-1">{data?.totalBranches || "0"}</h2>
            </div>

            {/* Card 2 - Branch network total + central warehouse context */}
            <div className="bg-surface-container-lowest p-6 rounded-xl hover:-translate-y-1 hover:shadow-[0px_24px_48px_rgba(44,47,49,0.06)] transition-all duration-300">
              <div className="flex justify-between items-center mb-4">
                <div className="p-2 bg-surface-container-low rounded-lg">
                  <span className="material-symbols-outlined text-primary">inventory_2</span>
                </div>
                <div className="flex items-center gap-1 bg-primary-container/20 px-2 py-1 rounded-full">
                  <span className="text-[10px] font-bold text-on-primary-container uppercase tracking-tighter">BRANCH STOCK</span>
                </div>
              </div>
              <p className="text-on-surface-variant text-sm font-medium">Total units (all branches)</p>
              <h2 className="text-3xl font-black text-on-surface tracking-tighter mt-1">
                {Math.round(data?.totalBranchInventoryUnits ?? 0).toLocaleString()}{" "}
                <span className="text-sm font-medium text-on-surface-variant italic">units</span>
              </h2>
              <p className="text-on-surface-variant text-xs font-medium mt-3 leading-snug">
                Central warehouse ({data?.warehouseName || "Main"}):{" "}
                <span className="text-on-surface font-bold">
                  {Math.round(data?.warehouseStock ?? 0).toLocaleString()} units
                </span>
              </p>
            </div>

            {/* Card 3 - Replaced with Pending Request */}
            <div className="bg-surface-container-lowest p-6 rounded-xl hover:-translate-y-1 hover:shadow-[0px_24px_48px_rgba(44,47,49,0.06)] transition-all duration-300 border-l-4 border-primary">
              <div className="flex justify-between items-start mb-4">
                <div className="p-2 bg-primary/10 rounded-lg">
                  <span className="material-symbols-outlined text-primary">sync_alt</span>
                </div>
              </div>
              <p className="text-on-surface-variant text-sm font-medium">Pending Requests</p>
              <h2 className="text-3xl font-black text-on-surface tracking-tighter mt-1">{pendingRequests.length}</h2>
              <div className="mt-4 flex items-center gap-1">
                <span className="text-[10px] font-bold text-primary uppercase tracking-widest">Branch Stock Requests</span>
              </div>
            </div>

            {/* Card 4 - Expired Product Count */}
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
              <p className="text-on-surface-variant text-sm font-medium">Out of Stock Batches</p>
              <h2 className="text-3xl font-black text-on-surface tracking-tighter mt-1">{data?.outOfStockCount.toLocaleString() || "0"}</h2>
            </div>

            {/* Card 4 - Replaced with Monthly Food Waste */}
            <div className="bg-surface-container-lowest p-6 rounded-xl hover:-translate-y-1 hover:shadow-[0px_24px_48px_rgba(44,47,49,0.06)] transition-all duration-300">
              <div className="flex justify-between items-start mb-4">
                <div className="p-2 bg-surface-container-low rounded-lg">
                  <span className="material-symbols-outlined text-warning">warning</span>
                </div>
                <div className="flex items-center gap-1 bg-warning-container/20 px-2 py-1 rounded-full">
                  <span className="text-[10px] font-bold text-on-warning-container">ACTION NEEDED</span>
                </div>
              </div>
              <p className="text-on-surface-variant text-sm font-medium">Low Stock Batches</p>
              <h2 className="text-3xl font-black text-on-surface tracking-tighter mt-1">{data?.lowStockCount.toLocaleString() || "0"}</h2>
            </div>

            <div className="bg-surface-container-lowest p-6 rounded-xl hover:-translate-y-1 hover:shadow-[0px_24px_48px_rgba(44,47,49,0.06)] transition-all duration-300">
              <div className="flex justify-between items-start mb-4">
                <div className="p-2 bg-surface-container-low rounded-lg">
                  <span className="material-symbols-outlined text-warning">timer</span>
                </div>
                <div className="flex items-center gap-1 bg-warning-container/20 px-2 py-1 rounded-full">
                  <span className="text-[10px] font-bold text-on-warning-container uppercase">within 7 days</span>
                </div>
              </div>
              <p className="text-on-surface-variant text-sm font-medium">Soon Expiring</p>
              <h2 className="text-3xl font-black text-on-surface tracking-tighter mt-1">{data?.expiringCount || "0"}</h2>
            </div>

            {/* Card 8 - Total Recipes */}
            <div className="bg-surface-container-lowest p-6 rounded-xl hover:-translate-y-1 hover:shadow-[0px_24px_48px_rgba(44,47,49,0.06)] transition-all duration-300">
              <div className="flex justify-between items-start mb-4">
                <div className="p-2 bg-surface-container-low rounded-lg">
                  <span className="material-symbols-outlined text-primary">inventory</span>
                </div>
              </div>
              <p className="text-on-surface-variant text-sm font-medium">Products in Inventory</p>
              <h2 className="text-3xl font-black text-on-surface tracking-tighter mt-1">{data?.totalProducts || "0"}</h2>
            </div>

            {/* Card 8 - Total Food Wastage */}
            <div className="bg-surface-container-lowest p-6 rounded-xl hover:-translate-y-1 hover:shadow-[0px_24px_48px_rgba(44,47,49,0.06)] transition-all duration-300">
              <div className="flex justify-between items-start mb-4">
                <div className="p-2 bg-surface-container-low rounded-lg">
                  <span className="material-symbols-outlined text-error">delete_sweep</span>
                </div>
              </div>
              <p className="text-on-surface-variant text-sm font-medium">Total Food Wastage</p>
              <h2 className="text-3xl font-black text-on-surface tracking-tighter mt-1">{data?.foodWastage.toLocaleString() || "0"} <span className="text-sm font-medium text-on-surface-variant">units</span></h2>
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
                {(data?.orderSummary || []).map((item, idx) => (
                  <div className="w-full bg-surface-container-low rounded-t-xl transition-all duration-700 relative" style={{ height: `${(item.value / 100) * 100}%` }} key={idx}>
                    <div className="absolute inset-x-0 bottom-0 neon-gradient h-1/2 opacity-20 rounded-t-xl" />
                  </div>
                ))}
              </div>
              <div className="flex justify-between mt-4 px-2 text-[10px] font-bold text-on-surface-variant tracking-widest uppercase">
                {(data?.orderSummary || []).map((item, idx) => <span key={idx}>{item.day}</span>)}
              </div>
            </div>

            {/* Profit by Category Donut Chart */}
            <div className="bg-surface-container-lowest p-8 rounded-xl flex flex-col items-center justify-center">
              <h3 className="text-xl font-bold tracking-tight text-on-surface mb-2 self-start">Profit by Category</h3>
              <p className="text-on-surface-variant text-sm mb-8 self-start">Total: ₹{data?.totalProfit.toLocaleString() || "0"} this period</p>
              <div className="relative w-48 h-48 flex items-center justify-center">
                <svg className="w-full h-full transform -rotate-90">
                  <circle className="text-surface-container-low" cx="96" cy="96" fill="transparent" r="80" stroke="currentColor" strokeWidth="20" />
                  <circle className="text-primary" cx="96" cy="96" fill="transparent" r="80" stroke="currentColor" strokeDasharray="502" strokeDashoffset="150" strokeWidth="20" />
                  <circle className="text-on-surface" cx="96" cy="96" fill="transparent" r="80" stroke="currentColor" strokeDasharray="502" strokeDashoffset="400" strokeWidth="20" />
                </svg>
                <div className="absolute flex flex-col items-center">
                  <span className="text-2xl font-black tracking-tighter">{data?.overallPercentage || "0"}%</span>
                  <span className="text-[10px] font-bold text-on-surface-variant uppercase tracking-widest">Growth</span>
                </div>
              </div>
              <div className="w-full mt-8 space-y-3">
                {(data?.profitByCategory || []).map((item, idx) => (
                  <div className="flex justify-between items-center" key={idx}>
                    <div className="flex items-center gap-2">
                      <span className={`w-2 h-2 rounded-full ${item.color === 'primary' ? 'bg-primary' : item.color === 'on-surface' ? 'bg-on-surface' : 'bg-surface-dim'}`} />
                      <span className="text-xs font-medium">{item.name}</span>
                    </div>
                    <span className="text-xs font-bold">{item.percent}%</span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Bottom Section: Stock Levels & Activity & Requests */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Stock Levels Progress Bars */}
            <div className="bg-surface-container-lowest p-8 rounded-xl">
              <div className="flex justify-between items-center mb-6">
                <h3 className="text-xl font-bold tracking-tight text-on-surface">Critical Stock Levels</h3>
                <button className="text-xs font-bold text-primary hover:underline">View All Inventory</button>
              </div>
              <div className="space-y-6">
                {(data?.stockLevels && data.stockLevels.length > 0) ? data.stockLevels.map((item, idx) => (
                  <div className="group" key={idx}>
                    <div className="flex justify-between text-sm mb-2">
                      <span className="font-medium text-on-surface">{item.name}</span>
                      <span className="font-bold text-error">{item.left} left</span>
                    </div>
                    <div className="w-full bg-surface-container-low h-2 rounded-full overflow-hidden">
                      <div className="bg-error h-full transition-all duration-1000" style={{ width: `${(item.left / (item.total || 100)) * 100}%` }} />
                    </div>
                  </div>
                )) : (
                  <p className="text-on-surface-variant text-sm">No critical stock alerts.</p>
                )}
              </div>
            </div>

            

            {/* Recent Activity */}
            <div className="bg-surface-container-lowest p-8 rounded-xl">
              <h3 className="text-xl font-bold tracking-tight text-on-surface mb-6">Live Operations</h3>
              <div className="space-y-6">
                {(data?.recentActivity && data.recentActivity.length > 0) ? data.recentActivity.map((log, idx) => (
                  <div className="flex items-center gap-4" key={idx}>
                    <div className="w-10 h-10 rounded-full overflow-hidden bg-primary-container flex items-center justify-center">
                      <span className="material-symbols-outlined text-primary">history</span>
                    </div>
                    <div className="flex-1">
                      <p className="text-sm font-medium text-on-surface"><span className="font-bold">{log.action}</span>: {log.details}</p>
                      <p className="text-[10px] text-on-surface-variant font-bold tracking-widest uppercase">
                        {new Date(log.timestamp).toLocaleTimeString()}
                      </p>
                    </div>
                  </div>
                )) : (
                  <p className="text-on-surface-variant text-sm">No recent activity.</p>
                )}
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

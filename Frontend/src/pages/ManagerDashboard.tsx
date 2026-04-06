import { useState, useEffect, useCallback, useRef } from "react";
import { useNavigate } from "react-router-dom";

const API_BASE = "http://localhost:8080/Inventa/api";

function getGreeting() {
  const hour = new Date().getHours();
  if (hour < 12) return "Good Morning";
  if (hour < 17) return "Good Afternoon";
  return "Good Evening";
}

interface BranchInventory {
  id: number;
  ingredientName: string;
  quantity: number;
  unit: string;
  threshold: number;
  imageUrl?: string;
}

interface LowStockAlert {
  id: number;
  ingredientName: string;
  currentQuantity: number;
  threshold: number;
  branchName?: string;
}

interface StockRequest {
  id: number;
  productName?: string;
  ingredientName?: string;
  quantity?: number;
  requestedQuantity?: string;
  status: string;
  createdAt?: string;
  requestedAt?: string;
  requestedBy?: { name: string };
}

interface AuditLog {
  id: number;
  action: string;
  performedBy?: string;
  details?: string;
  timestamp?: string;
  createdAt?: string;
}

// ── Ingredient icon map (fallback emojis by keyword) ─────────────────────────
const INGREDIENT_ICON: Record<string, string> = {
  tomato: "🍅", onion: "🧅", garlic: "🧄", potato: "🥔", carrot: "🥕",
  chicken: "🍗", beef: "🥩", fish: "🐟", egg: "🥚", milk: "🥛",
  flour: "🌾", sugar: "🍚", salt: "🧂", oil: "🫙", butter: "🧈",
  cheese: "🧀", rice: "🍚", wheat: "🌾", corn: "🌽", pepper: "🌶️",
};
function getIngredientEmoji(name: string) {
  const lower = name.toLowerCase();
  for (const key of Object.keys(INGREDIENT_ICON)) {
    if (lower.includes(key)) return INGREDIENT_ICON[key];
  }
  return "🥄";
}

// ── Stock-level helpers ───────────────────────────────────────────────────────
function stockLevel(qty: number, threshold: number): "critical" | "low" | "ok" {
  if (qty <= threshold * 0.5) return "critical";
  if (qty <= threshold) return "low";
  return "ok";
}
const LEVEL_STYLES = {
  critical: { bar: "bg-[#f95630]", badge: "bg-[#f95630]/10 text-[#b02500]", label: "Critical" },
  low: { bar: "bg-[#e6ea5a]", badge: "bg-[#e6ea5a]/20 text-[#535500]", label: "Low" },
  ok: { bar: "bg-[#c5fe3c]", badge: "bg-[#c5fe3c]/20 text-[#364b00]", label: "Healthy" },
};

export default function ManagerDashboard() {
  const navigate = useNavigate();
  const [managerName, setManagerName] = useState("Manager");
  const [branchName, setBranchName] = useState("Main Branch");
  const [branchId, setBranchId] = useState<number | null>(null);
  const [auth, setAuth] = useState("");
  const [activeTab, setActiveTab] = useState("dashboard");

  // Data state
  const [inventory, setInventory] = useState<BranchInventory[]>([]);
  const [lowStockAlerts, setLowStockAlerts] = useState<LowStockAlert[]>([]);
  const [stockRequests, setStockRequests] = useState<StockRequest[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);

  // Stock update form
  const [selectedIngredient, setSelectedIngredient] = useState("");
  const [updateQty, setUpdateQty] = useState<number>(0);
  const [updateAction, setUpdateAction] = useState<"add" | "reduce">("add");
  const [isUpdating, setIsUpdating] = useState(false);

  // Stock request form
  const [reqIngredient, setReqIngredient] = useState("");
  const [reqQuantity, setReqQuantity] = useState<number | "">("");
  const [reqUnit, setReqUnit] = useState("kg");
  const [isRequesting, setIsRequesting] = useState(false);

  // Inventory search
  const [inventorySearch, setInventorySearch] = useState("");

  // Add New Ingredient modal
  const [showAddModal, setShowAddModal] = useState(false);
  const [newName, setNewName] = useState("");
  const [newQty, setNewQty] = useState<number>(0);
  const [newUnit, setNewUnit] = useState("kg");
  const [newThreshold, setNewThreshold] = useState<number>(10);
  const [newImage, setNewImage] = useState<string | null>(null);
  const [isAdding, setIsAdding] = useState(false);
  const [editingItem, setEditingItem] = useState<BranchInventory | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const userStr = localStorage.getItem("user");
    if (!userStr) { navigate("/"); return; }
    const user = JSON.parse(userStr);
    const name = user.name || user.username || user.fullName || user.email?.split("@")[0] || "Manager";
    setManagerName(name);
    if (user.branch?.name) setBranchName(user.branch.name);
    else if (user.branchName) setBranchName(user.branchName);
    if (user.branch?.id) setBranchId(Number(user.branch.id));
    else if (user.branchId) setBranchId(Number(user.branchId));
    if (user.auth) setAuth(user.auth);
  }, [navigate]);

  const headers = useCallback(() => ({
    "Content-Type": "application/json",
    "Authorization": `Basic ${auth}`
  }), [auth]);

  const fetchAll = useCallback(async () => {
    if (!auth) return;
    setLoading(true);
    try {
      const [inventoryRes, alertsRes, requestsRes, auditRes] = await Promise.allSettled([
        branchId ? fetch(`${API_BASE}/branch-inventory/branch/${branchId}`, { headers: headers() }) : Promise.reject("no branch"),
        fetch(`${API_BASE}/alerts/manager/open`, { headers: headers() }),
        fetch(`${API_BASE}/stock-requests/my-branch`, { headers: headers() }),
        fetch(`${API_BASE}/audit`, { headers: headers() }),
      ]);
      if (inventoryRes.status === "fulfilled" && inventoryRes.value.ok) setInventory(await inventoryRes.value.json());
      if (alertsRes.status === "fulfilled" && alertsRes.value.ok) setLowStockAlerts(await alertsRes.value.json());
      if (requestsRes.status === "fulfilled" && requestsRes.value.ok) setStockRequests(await requestsRes.value.json());
      if (auditRes.status === "fulfilled" && auditRes.value.ok) setAuditLogs(await auditRes.value.json());
    } catch (err) {
      console.error("Fetch error", err);
    } finally {
      setLoading(false);
    }
  }, [auth, branchId, headers]);

  useEffect(() => { fetchAll(); }, [fetchAll]);

  const handleUpdateStock = async () => {
    if (!selectedIngredient || !updateQty || !branchId) return;
    setIsUpdating(true);
    try {
      const current = inventory.find(i => i.ingredientName === selectedIngredient);
      const newQty = updateAction === "add"
        ? (current?.quantity || 0) + updateQty
        : Math.max(0, (current?.quantity || 0) - updateQty);
      await fetch(`${API_BASE}/branch-inventory`, {
        method: "POST",
        headers: headers(),
        body: JSON.stringify({ branchId, ingredientName: selectedIngredient, quantity: newQty, unit: current?.unit || "units", threshold: current?.threshold || 10 })
      });
      await fetchAll();
      setUpdateQty(0);
    } catch (err) { console.error(err); }
    finally { setIsUpdating(false); }
  };

  const handleStockRequest = async () => {
    if (!reqIngredient || !reqQuantity || !branchId) return;
    setIsRequesting(true);
    try {
      const res = await fetch(`${API_BASE}/stock-requests`, {
        method: "POST",
        headers: headers(),
        body: JSON.stringify({ branchId, ingredientName: reqIngredient, quantity: reqQuantity, unit: reqUnit })
      });
      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        alert(errData.error || "Failed to submit request.");
        return;
      }
      await fetchAll();
      setReqIngredient(""); setReqQuantity("");
    } catch (err) { console.error(err); }
    finally { setIsRequesting(false); }
  };

  const handleReceiveRequest = async (id: number) => {
    try {
      const res = await fetch(`${API_BASE}/stock-requests/${id}/receive`, {
        method: "POST",
        headers: headers()
      });
      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        alert(errData.error || "Failed to confirm receipt.");
        return;
      }
      await fetchAll();
    } catch (err) {
      console.error(err);
    }
  };

  const handleDeleteInventory = async (id: number) => {
    if (!window.confirm("Are you sure you want to delete this ingredient?")) return;
    try {
      const res = await fetch(`${API_BASE}/branch-inventory/${id}`, {
        method: "DELETE",
        headers: headers()
      });
      if (res.ok) await fetchAll();
      else alert("Failed to delete item.");
    } catch (err) { console.error(err); }
  };

  const openEditModal = (item: BranchInventory) => {
    setEditingItem(item);
    setNewName(item.ingredientName);
    setNewQty(item.quantity);
    setNewUnit(item.unit);
    setNewThreshold(item.threshold);
    setNewImage(item.imageUrl || null);
    setShowAddModal(true);
  };

  const handleAddIngredient = async () => {
    if (!newName || (newQty === undefined || newQty === null) || !branchId) return;
    setIsAdding(true);
    try {
      const payload = {
        id: editingItem?.id, // include ID if editing
        branchId,
        ingredientName: newName,
        quantity: newQty,
        unit: newUnit,
        threshold: newThreshold
      };

      await fetch(`${API_BASE}/branch-inventory`, {
        method: "POST",
        headers: headers(),
        body: JSON.stringify(payload)
      });
      await fetchAll();
      // reset form
      setNewName(""); setNewQty(0); setNewUnit("kg"); setNewThreshold(10); setNewImage(null);
      setEditingItem(null);
      setShowAddModal(false);
    } catch (err) { console.error(err); }
    finally { setIsAdding(false); }
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = ev => setNewImage(ev.target?.result as string);
    reader.readAsDataURL(file);
  };

  const handleLogout = () => { localStorage.removeItem("user"); navigate("/"); };

  const navItems = [
    { id: "dashboard", icon: "dashboard", label: "Dashboard" },
    { id: "inventory", icon: "inventory_2", label: "Branch Inventory" },
    { id: "update-stock", icon: "edit_calendar", label: "Update Ingredients Stock" },
    { id: "orders", icon: "shopping_cart", label: "Orders" },
    { id: "stock-requests", icon: "rebase_edit", label: "Stock Requests" },
    { id: "reports", icon: "assessment", label: "Reports" },
    { id: "staff", icon: "group", label: "Staff" },
  ];

  const greeting = getGreeting();
  const totalStock = inventory.reduce((sum, i) => sum + i.quantity, 0);
  const pendingRequests = stockRequests.filter(r => r.status === "PENDING").length;

  const formatDate = (d?: string) => {
    if (!d) return "—";
    return new Date(d).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
  };

  const statusBadge = (status: string) => {
    const map: Record<string, string> = {
      PENDING: "bg-[#e6ea5a] text-[#535500]", APPROVED: "bg-[#c5fe3c] text-[#455f00]",
      DISPATCHED: "bg-[#2563eb] text-white", DELIVERED: "bg-[#455f00] text-white",
      FULFILLED: "bg-[#dadde0] text-[#595c5e]", REJECTED: "bg-[#f95630] text-white",
    };
    return map[status?.toUpperCase()] || "bg-[#eff1f3] text-[#595c5e]";
  };

  const filteredInventory = inventory.filter(i =>
    i.ingredientName.toLowerCase().includes(inventorySearch.toLowerCase())
  );

  // ── Page title helper ──────────────────────────────────────────────────────
  const pageTitle: Record<string, string> = {
    dashboard: `${greeting}, ${managerName}`,
    inventory: "Branch Inventory",
    "update-stock": "Update Ingredients Stock",
    orders: "Orders",
    "stock-requests": "Stock Requests",
    reports: "Reports",
    staff: "Staff",
  };

  // ── Render ─────────────────────────────────────────────────────────────────
  return (
    <div className="bg-[#f5f6f8] text-[#2c2f31] min-h-screen font-['Inter'] antialiased">

      {/* ── Add New Ingredient Modal ─────────────────────────────────────── */}
      {showAddModal && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/60 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 overflow-hidden animate-[fadeInUp_0.25s_ease]">
            {/* Modal Header */}
            <div className="bg-[#0c0f10] px-8 py-6 flex justify-between items-center">
              <div>
                <h2 className="text-white font-black text-lg tracking-tight">{editingItem ? "Edit Ingredient" : "Add New Ingredient"}</h2>
                <p className="text-white/40 text-xs mt-0.5">{editingItem ? "Update record for" : "Add a new item to"} {branchName}</p>
              </div>
              <button onClick={() => { setShowAddModal(false); setEditingItem(null); }} className="text-white/50 hover:text-white transition-colors">
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>

            {/* Modal Body */}
            <div className="p-8 space-y-5">
              {/* Image Upload */}
              <div
                onClick={() => fileInputRef.current?.click()}
                className="relative w-full h-36 rounded-xl border-2 border-dashed border-[#c5fe3c]/40 bg-[#f5f6f8] flex flex-col items-center justify-center cursor-pointer hover:border-[#c5fe3c] hover:bg-[#c5fe3c]/5 transition-all group overflow-hidden"
              >
                {newImage ? (
                  <img src={newImage} alt="preview" className="w-full h-full object-cover rounded-xl" />
                ) : (
                  <>
                    <span className="material-symbols-outlined text-4xl text-[#c5fe3c] group-hover:scale-110 transition-transform">add_photo_alternate</span>
                    <p className="text-xs text-[#595c5e] mt-2 font-medium">Click to upload ingredient image</p>
                    <p className="text-[10px] text-[#abadaf]">PNG, JPG, WEBP up to 5MB</p>
                  </>
                )}
                {newImage && (
                  <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-all flex items-center justify-center rounded-xl">
                    <span className="material-symbols-outlined text-white text-3xl">edit</span>
                  </div>
                )}
              </div>
              <input ref={fileInputRef} type="file" accept="image/*" className="hidden" onChange={handleImageChange} />

              {/* Name */}
              <div>
                <label className="block text-[11px] font-bold text-[#595c5e] uppercase tracking-widest mb-1.5">Ingredient Name *</label>
                <input
                  value={newName} onChange={e => setNewName(e.target.value)}
                  className="w-full bg-[#eff1f3] border-none rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-[#c5fe3c] outline-none"
                  placeholder="e.g., Tomatoes"
                />
              </div>

              {/* Qty + Unit */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-[11px] font-bold text-[#595c5e] uppercase tracking-widest mb-1.5">Quantity *</label>
                  <input
                    value={newQty || ""} onChange={e => setNewQty(Number(e.target.value))}
                    type="number" min={0}
                    className="w-full bg-[#eff1f3] border-none rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-[#c5fe3c] outline-none"
                    placeholder="0"
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-bold text-[#595c5e] uppercase tracking-widest mb-1.5">Unit</label>
                  <select
                    value={newUnit} onChange={e => setNewUnit(e.target.value)}
                    className="w-full bg-[#eff1f3] border-none rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-[#c5fe3c] outline-none"
                  >
                    {["kg", "g", "L", "mL", "units", "pcs", "boxes"].map(u => <option key={u}>{u}</option>)}
                  </select>
                </div>
              </div>

              {/* Threshold */}
              <div>
                <label className="block text-[11px] font-bold text-[#595c5e] uppercase tracking-widest mb-1.5">Low-Stock Threshold</label>
                <input
                  value={newThreshold || ""} onChange={e => setNewThreshold(Number(e.target.value))}
                  type="number" min={0}
                  className="w-full bg-[#eff1f3] border-none rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-[#c5fe3c] outline-none"
                  placeholder="10"
                />
              </div>
            </div>

            {/* Modal Footer */}
            <div className="px-8 pb-8 flex gap-3">
              <button onClick={() => { setShowAddModal(false); setEditingItem(null); }} className="flex-1 py-3 rounded-xl border border-black/10 text-sm font-semibold hover:bg-[#eff1f3] transition-all">
                Cancel
              </button>
              <button
                onClick={handleAddIngredient}
                disabled={isAdding || !newName || (newQty === undefined || newQty === null)}
                className="flex-1 py-3 rounded-xl bg-[#c5fe3c] text-[#364b00] text-sm font-black shadow-[0_4px_14px_0_rgba(197,254,60,0.4)] hover:-translate-y-0.5 transition-all disabled:opacity-50"
              >
                {isAdding ? (editingItem ? "Updating..." : "Adding...") : (editingItem ? "Update Record" : "Add Ingredient")}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Sidebar ──────────────────────────────────────────────────────── */}
      <aside className="h-screen w-64 fixed left-0 top-0 bg-[#0c0f10] flex flex-col py-8 px-4 z-50 shadow-[0px_24px_48px_rgba(44,47,49,0.12)]">
        <div className="mb-12 px-4">
          <h1 className="text-2xl font-black text-[#c5fe3c] tracking-tighter">Ventorie</h1>
          <p className="text-[10px] text-white/30 uppercase tracking-widest mt-1">Branch Hub</p>
        </div>
        <nav className="flex-1 space-y-1">
          {navItems.map(item => (
            <button key={item.id} onClick={() => setActiveTab(item.id)}
              className={`w-full flex items-center gap-3 px-4 py-3 text-left transition-all duration-300 ${activeTab === item.id
                  ? "text-[#c5fe3c] border-l-4 border-[#496400] bg-gradient-to-r from-[#496400]/10 to-transparent font-bold tracking-tight translate-x-1"
                  : "text-gray-400 hover:text-white hover:bg-white/5 font-medium tracking-tight border-l-4 border-transparent"
                }`}>
              <span className="material-symbols-outlined text-lg">{item.icon}</span>
              <span className="text-sm">{item.label}</span>
            </button>
          ))}
        </nav>
        <div className="mt-auto border-t border-white/10 pt-4">
          <button onClick={handleLogout} className="w-full flex items-center gap-3 px-4 py-3 text-gray-400 hover:text-white hover:bg-white/5 transition-all">
            <span className="material-symbols-outlined text-lg">logout</span>
            <span className="text-sm font-medium">Logout</span>
          </button>
        </div>
      </aside>

      {/* ── Main ─────────────────────────────────────────────────────────── */}
      <main className="ml-64 min-h-screen pb-12">
        {/* Top Bar */}
        <header className="flex justify-between items-center px-6 py-3 mx-6 mt-4 rounded-xl bg-white/80 backdrop-blur-md shadow-sm border border-black/5">
          <div className="flex items-center bg-[#eff1f3] px-4 py-2 rounded-full w-96">
            <span className="material-symbols-outlined text-[#595c5e] mr-2 text-xl">search</span>
            <input
              className="bg-transparent border-none focus:ring-0 text-sm w-full p-0 outline-none placeholder-[#abadaf]"
              placeholder="Search inventory..."
              value={inventorySearch}
              disabled={activeTab !== "inventory"}
              onChange={e => setInventorySearch(e.target.value)}
              type="text"
            />
          </div>
          <div className="flex items-center gap-6">
            <button className="relative text-[#595c5e] hover:bg-[#eff1f3] p-2 rounded-full transition-colors">
              <span className="material-symbols-outlined">notifications</span>
              {lowStockAlerts.length > 0 && <span className="absolute top-2 right-2 w-2 h-2 bg-[#b02500] rounded-full"></span>}
            </button>
            <div className="flex items-center gap-3">
              <div className="text-right">
                <p className="text-sm font-bold tracking-tight">{managerName}</p>
                <p className="text-[10px] text-[#595c5e] font-medium uppercase tracking-widest">{branchName}</p>
              </div>
              <div className="w-10 h-10 rounded-full bg-[#c5fe3c] flex items-center justify-center font-black text-[#364b00] text-lg shadow-sm">
                {managerName.charAt(0).toUpperCase()}
              </div>
            </div>
          </div>
        </header>

        {/* Content Body */}
        <div className="px-8 mt-8">

          {/* Page heading */}
          <div className="flex justify-between items-end mb-8">
            <div>
              {activeTab === "dashboard" ? (
                <>
                  <h2 className="text-3xl font-semibold tracking-tight">
                    <span className="text-[#0c0f10] font-black">{greeting},</span>{" "}
                    <span className="font-black text-[#496400]">{managerName}</span>
                  </h2>
                  <p className="text-[#595c5e] mt-1 font-medium">
                    Here's what's happening at <span className="font-bold text-[#0c0f10]">{branchName}</span> today.
                  </p>
                </>
              ) : (
                <>
                  <h2 className="text-3xl font-black tracking-tight text-[#0c0f10]">{pageTitle[activeTab]}</h2>
                  <p className="text-[#595c5e] mt-1 font-medium">{branchName}</p>
                </>
              )}
            </div>
            <div className="flex gap-3">
              {activeTab === "inventory" && (
                <button
                  id="add-ingredient-btn"
                  onClick={() => setShowAddModal(true)}
                  className="bg-[#c5fe3c] text-[#364b00] py-2.5 px-6 rounded-xl font-bold text-sm shadow-[0_4px_14px_0_rgba(197,254,60,0.4)] hover:-translate-y-0.5 transition-all flex items-center gap-2"
                >
                  <span className="material-symbols-outlined text-base">add</span>
                  Add New Ingredient
                </button>
              )}
              {activeTab === "dashboard" && (
                <>
                  <button className="bg-white text-[#496400] py-2.5 px-6 rounded-xl font-semibold text-sm hover:-translate-y-0.5 transition-all border border-black/5 shadow-sm">Export Report</button>
                  <button className="bg-[#c5fe3c] text-[#364b00] py-2.5 px-6 rounded-xl font-bold text-sm shadow-[0_4px_14px_0_rgba(197,254,60,0.4)] hover:-translate-y-0.5 transition-all">New Order</button>
                </>
              )}
            </div>
          </div>

          {/* ══════════════════════════════════════════════════════════════
              TAB: DASHBOARD
          ══════════════════════════════════════════════════════════════ */}
          {activeTab === "dashboard" && (
            <>
              {/* Stats Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
                <div className="bg-white p-6 rounded-xl hover:-translate-y-1 transition-all shadow-sm border border-black/5">
                  <div className="flex justify-between items-start mb-4">
                    <div className="p-2 bg-[#496400]/10 rounded-lg"><span className="material-symbols-outlined text-[#496400]">inventory</span></div>
                    <span className="text-[11px] font-bold py-1 px-2 bg-[#455f00] text-[#deff95] rounded-full">{inventory.length} items</span>
                  </div>
                  <p className="text-[#595c5e] text-sm font-medium">Available Stock</p>
                  <h3 className="text-2xl font-bold mt-1">{loading ? "—" : `${totalStock.toLocaleString()} units`}</h3>
                </div>

                <div className="bg-white p-6 rounded-xl hover:-translate-y-1 transition-all shadow-sm border border-black/5">
                  <div className="flex justify-between items-start mb-4">
                    <div className="p-2 bg-[#b02500]/10 rounded-lg"><span className="material-symbols-outlined text-[#b02500]">warning</span></div>
                    {lowStockAlerts.length > 0 && <span className="text-[11px] font-bold py-1 px-2 bg-[#b02500] text-white rounded-full">Action required</span>}
                  </div>
                  <p className="text-[#595c5e] text-sm font-medium">Low Stock Alerts</p>
                  <h3 className="text-2xl font-bold mt-1">{loading ? "—" : `${lowStockAlerts.length} items`}</h3>
                </div>

                <div className="bg-white p-6 rounded-xl hover:-translate-y-1 transition-all shadow-sm border border-black/5">
                  <div className="flex justify-between items-start mb-4">
                    <div className="p-2 bg-[#5d5f00]/10 rounded-lg"><span className="material-symbols-outlined text-[#5d5f00]">shopping_basket</span></div>
                  </div>
                  <p className="text-[#595c5e] text-sm font-medium">Stock Requests</p>
                  <h3 className="text-2xl font-bold mt-1">{loading ? "—" : `${stockRequests.length} total`}</h3>
                </div>

                <div className="bg-white p-6 rounded-xl hover:-translate-y-1 transition-all shadow-sm border border-black/5 border-l-4 border-l-[#c5fe3c]">
                  <div className="flex justify-between items-start mb-4">
                    <div className="p-2 bg-[#eff1f3] rounded-lg"><span className="material-symbols-outlined text-[#595c5e]">pending_actions</span></div>
                  </div>
                  <p className="text-[#595c5e] text-sm font-medium">Pending Requests</p>
                  <h3 className="text-2xl font-bold mt-1">{loading ? "—" : `${pendingRequests} requests`}</h3>
                </div>
              </div>

              {/* Bento Grid */}
              <div className="grid grid-cols-12 gap-8">
                <div className="col-span-12 lg:col-span-4 flex flex-col gap-8">
                  {/* Update Stock */}
                  <div className="bg-white p-8 rounded-xl shadow-sm border border-black/5">
                    <h3 className="text-lg font-bold tracking-tight mb-6">Update Ingredients Stock</h3>
                    <div className="space-y-6">
                      <div>
                        <label className="block text-[11px] font-bold text-[#595c5e] uppercase tracking-widest mb-2">Ingredient</label>
                        <select value={selectedIngredient} onChange={e => setSelectedIngredient(e.target.value)} className="w-full bg-[#eff1f3] border-none rounded-xl p-3 text-sm focus:ring-2 focus:ring-[#c5fe3c] outline-none">
                          <option value="">Select ingredient...</option>
                          {inventory.map(i => <option key={i.id} value={i.ingredientName}>{i.ingredientName} ({i.quantity} {i.unit})</option>)}
                        </select>
                      </div>
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <label className="block text-[11px] font-bold text-[#595c5e] uppercase tracking-widest mb-2">Quantity</label>
                          <input value={updateQty || ""} onChange={e => setUpdateQty(Number(e.target.value))} className="w-full bg-[#eff1f3] border-none rounded-xl p-3 text-sm focus:ring-2 focus:ring-[#c5fe3c] outline-none" placeholder="0" type="number" />
                        </div>
                        <div>
                          <label className="block text-[11px] font-bold text-[#595c5e] uppercase tracking-widest mb-2">Action</label>
                          <div className="flex bg-[#eff1f3] p-1 rounded-xl">
                            <button onClick={() => setUpdateAction("add")} className={`flex-1 text-xs font-bold py-2 rounded-lg transition-all ${updateAction === "add" ? "bg-white shadow-sm text-[#0c0f10]" : "text-[#595c5e]"}`} type="button">Add</button>
                            <button onClick={() => setUpdateAction("reduce")} className={`flex-1 text-xs font-bold py-2 rounded-lg transition-all ${updateAction === "reduce" ? "bg-white shadow-sm text-[#0c0f10]" : "text-[#595c5e]"}`} type="button">Reduce</button>
                          </div>
                        </div>
                      </div>
                      <button onClick={handleUpdateStock} disabled={isUpdating || !selectedIngredient} className="w-full bg-[#496400] text-white py-4 rounded-xl font-bold text-sm hover:-translate-y-0.5 transition-all shadow-sm disabled:opacity-50">
                        {isUpdating ? "Updating..." : "Update Stock"}
                      </button>
                    </div>
                  </div>
                </div>

                {/* Right Column */}
                <div className="col-span-12 lg:col-span-8 flex flex-col gap-8">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    {/* Low Stock Alerts */}
                    <div className="bg-white p-6 rounded-xl shadow-sm border border-black/5">
                      <div className="flex items-center gap-2 mb-6">
                        <span className="material-symbols-outlined text-[#b02500]">report</span>
                        <h3 className="text-lg font-bold tracking-tight">Low Stock Alerts</h3>
                      </div>
                      {loading ? (
                        <p className="text-sm text-[#abadaf] text-center py-4">Loading alerts...</p>
                      ) : lowStockAlerts.length === 0 ? (
                        <div className="text-center py-8 border-2 border-dashed border-[#c5fe3c]/20 rounded-xl">
                          <span className="material-symbols-outlined text-[#496400] text-3xl">check_circle</span>
                          <p className="text-sm text-[#595c5e] font-medium mt-2">All stock levels are healthy!</p>
                        </div>
                      ) : (
                        <div className="space-y-4 max-h-[250px] overflow-y-auto pr-1">
                          {lowStockAlerts.map(alert => (
                            <div key={alert.id} className="flex justify-between items-center p-4 bg-[#b02500]/5 rounded-xl border-l-4 border-[#b02500]">
                              <div>
                                <p className="text-sm font-bold">{alert.ingredientName}</p>
                                <p className="text-xs text-[#b02500] font-medium">{alert.currentQuantity} left (Threshold: {alert.threshold})</p>
                              </div>
                              <span className="text-[10px] font-black px-2 py-1 bg-[#b02500] text-white rounded-lg">CRITICAL</span>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>

                    {/* Activity Panel */}
                    <div className="bg-white p-6 rounded-xl shadow-sm border border-black/5">
                      <h3 className="text-lg font-bold tracking-tight mb-6">Activity Panel</h3>
                      {loading ? (
                        <p className="text-sm text-[#abadaf] text-center py-4">Loading activity...</p>
                      ) : auditLogs.length === 0 ? (
                        <p className="text-sm text-[#abadaf] text-center py-4">No recent activity.</p>
                      ) : (
                        <div className="space-y-6 max-h-[250px] overflow-y-auto pr-1">
                          {auditLogs.slice(0, 5).map(log => (
                            <div key={log.id} className="flex gap-4">
                              <div className="w-8 h-8 rounded-full bg-[#c5fe3c] flex items-center justify-center flex-shrink-0">
                                <span className="material-symbols-outlined text-[#496400] text-sm">history</span>
                              </div>
                              <div>
                                <p className="text-sm font-medium">{log.action}</p>
                                {log.details && <p className="text-xs text-[#496400] font-bold">{log.details}</p>}
                                <p className="text-[10px] text-[#595c5e] mt-1">{formatDate(log.timestamp || log.createdAt)}</p>
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </>
          )}

          {/* ══════════════════════════════════════════════════════════════
              TAB: BRANCH INVENTORY
          ══════════════════════════════════════════════════════════════ */}
          {activeTab === "inventory" && (
            <>
              {/* Summary row */}
              <div className="flex gap-4 mb-8">
                {[
                  { label: "Total Items", value: inventory.length, icon: "inventory_2", color: "text-[#496400]", bg: "bg-[#496400]/10" },
                  { label: "Total Stock", value: `${totalStock.toLocaleString()} units`, icon: "scale", color: "text-[#5d5f00]", bg: "bg-[#5d5f00]/10" },
                  { label: "Low / Critical", value: inventory.filter(i => stockLevel(i.quantity, i.threshold) !== "ok").length, icon: "warning", color: "text-[#b02500]", bg: "bg-[#b02500]/10" },
                ].map(s => (
                  <div key={s.label} className="flex-1 bg-white rounded-xl p-5 flex items-center gap-4 shadow-sm border border-black/5">
                    <div className={`p-3 rounded-xl ${s.bg}`}>
                      <span className={`material-symbols-outlined ${s.color}`}>{s.icon}</span>
                    </div>
                    <div>
                      <p className="text-[11px] font-bold text-[#595c5e] uppercase tracking-widest">{s.label}</p>
                      <p className="text-2xl font-black tracking-tight">{s.value}</p>
                    </div>
                  </div>
                ))}
              </div>

              {/* ── Ingredient list table ── */}
              <div className="bg-white rounded-2xl shadow-sm border border-black/5 overflow-hidden">

                {/* Column headers */}
                <div className="grid grid-cols-[2fr_1.2fr_1.5fr_1.8fr_1.5fr_auto] gap-6 items-center px-8 py-4 border-b border-black/5 bg-[#f9fafb]">
                  {["Ingredient / ID", "Current Level", "Expiry Risk", "Usage Rate", "New Stock", "Branch Action"].map(h => (
                    <span key={h} className="text-[10px] font-black text-[#abadaf] uppercase tracking-widest whitespace-nowrap">{h}</span>
                  ))}
                </div>

                {loading ? (
                  <div className="flex flex-col items-center justify-center py-24 gap-3">
                    <span className="material-symbols-outlined text-5xl text-[#c5fe3c] animate-spin" style={{ animationDuration: "1.5s" }}>progress_activity</span>
                    <p className="text-[#595c5e] font-medium">Loading inventory...</p>
                  </div>
                ) : filteredInventory.length === 0 ? (
                  <div className="flex flex-col items-center justify-center py-24 gap-4">
                    <div className="w-20 h-20 rounded-2xl bg-[#eff1f3] flex items-center justify-center">
                      <span className="material-symbols-outlined text-4xl text-[#abadaf]">inventory_2</span>
                    </div>
                    <p className="text-[#595c5e] font-semibold">
                      {inventorySearch ? `No ingredients matching "${inventorySearch}"` : "No ingredients in this branch yet."}
                    </p>
                    <button
                      onClick={() => setShowAddModal(true)}
                      className="bg-[#c5fe3c] text-[#364b00] py-2.5 px-6 rounded-xl font-bold text-sm hover:-translate-y-0.5 transition-all flex items-center gap-2 shadow-[0_4px_14px_0_rgba(197,254,60,0.4)]"
                    >
                      <span className="material-symbols-outlined text-base">add</span>
                      Add First Ingredient
                    </button>
                  </div>
                ) : (
                  <div className="divide-y divide-black/[0.04]">
                    {filteredInventory.map((item) => {
                      const level = stockLevel(item.quantity, item.threshold);

                      const usageRate = Math.max(5, Math.min(95, Math.round((item.quantity / Math.max(item.threshold * 3, 1)) * 100)));

                      return (
                        <div
                          key={item.id}
                          className="grid grid-cols-[2fr_1.2fr_1.5fr_1.8fr_1.5fr_auto] gap-6 items-center px-8 py-6 hover:bg-[#fafafa] transition-colors border-b border-black-[0.02] last:border-b-0 group"
                        >
                          {/* Col 1 — Ingredient / ID */}
                          <div className="flex items-center gap-4">
                            <div className="relative w-12 h-12 rounded-xl bg-[#13171a] flex items-center justify-center overflow-hidden flex-shrink-0">
                              {level === "critical" && (
                                <span className="absolute top-1 right-0 text-[7px] font-black bg-[#c5fe3c] text-[#0c0f10] px-1 rounded-sm shadow-sm">NEW</span>
                              )}
                              <span className={`material-symbols-outlined text-[20px] ${level === "critical" ? "text-[#dc2626]" : "text-[#595c5e]"}`}>
                                {level === "critical" ? "shield_locked" : "inventory_2"}
                              </span>
                            </div>
                            <div className="min-w-0">
                              <p className="font-bold text-[#0c0f10] text-sm tracking-tight leading-tight truncate">{item.ingredientName}</p>
                              <p className="text-[10px] text-[#abadaf] font-mono mt-0.5">#{String(item.id).padStart(3, "0")} / BATCH001</p>
                              <p className="text-[10px] text-[#abadaf]">(GLOBAL HUB)</p>
                            </div>
                          </div>

                          {/* Col 2 — Current Level */}
                          <div>
                            <div className="flex items-center gap-1.5">
                              <p className={`text-sm font-black ${level === "critical" ? "text-[#dc2626]" : "text-[#2563eb]"
                                }`}>
                                {item.quantity} Units
                              </p>
                              <span className={`material-symbols-outlined text-sm font-bold ${level === "critical" ? "text-[#dc2626]" : "text-[#2563eb]"
                                }`}>
                                {level === "critical" ? "trending_down" : "trending_up"}
                              </span>
                            </div>
                          </div>

                          {/* Col 3 — Expiry Risk */}
                          <div>
                            <p className={`text-sm font-bold ${level === "critical" ? "text-[#dc2626]" : "text-[#0c0f10]"
                              }`}>
                              {level === "critical" ? "Expired" : "Low Risk"}
                            </p>
                            <p className={`text-[9px] font-black uppercase tracking-wider mt-0.5 ${level === "critical" ? "text-[#dc2626]" : "text-[#abadaf]"
                              }`}>
                              {level === "critical" ? "HAZARDOUS BATCH" : "STABLE"}
                            </p>
                          </div>

                          {/* Col 4 — Usage Rate */}
                          <div className="flex items-center gap-3">
                            <span className="text-[11px] font-bold text-[#0c0f10] tracking-tight">{usageRate}% / day</span>
                            <div className="h-1.5 bg-[#eff1f3] rounded-full overflow-hidden w-12">
                              <div className="h-full rounded-full bg-[#364b00]" style={{ width: `${usageRate}%` }} />
                            </div>
                          </div>

                          {/* Col 5 — New Stock */}
                          <div>
                            {level === "critical" ? (
                              <button className="bg-[#0c0f10] text-[#c5fe3c] text-[9px] font-black px-4 py-2.5 rounded-lg hover:bg-[#1f2937] transition-all tracking-widest uppercase">
                                ADD NEW STOCK
                              </button>
                            ) : (
                              <span className="text-[9px] font-black text-[#dadde0] tracking-widest uppercase">
                                REPLENISHED
                              </span>
                            )}
                          </div>

                          {/* Col 6 — Branch Action */}
                          <div className="flex items-center gap-4 justify-end transition-opacity">
                            <button 
                              onClick={() => openEditModal(item)}
                              className="text-[#3b82f6] hover:scale-110 transition-transform"
                            >
                              <span className="material-symbols-outlined text-[18px]">edit</span>
                            </button>
                            <button 
                              onClick={() => handleDeleteInventory(item.id)}
                              className="text-[#ef4444] hover:scale-110 transition-transform"
                            >
                              <span className="material-symbols-outlined text-[18px]">delete</span>
                            </button>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}

                {/* Footer / pagination */}
                {!loading && filteredInventory.length > 0 && (
                  <div className="px-6 py-4 border-t border-black/5 flex justify-between items-center bg-[#f9fafb]">
                    <p className="text-xs text-[#595c5e]">
                      Page <span className="font-bold text-[#0c0f10]">1</span> of{" "}
                      <span className="font-bold text-[#0c0f10]">{Math.ceil(filteredInventory.length / 10)}</span>{" "}
                      batches for <span className="font-bold text-[#496400]">{branchName}</span>
                    </p>
                    <div className="flex gap-1">
                      <button className="w-8 h-8 rounded-lg border border-black/10 flex items-center justify-center text-[#595c5e] hover:bg-white hover:border-[#c5fe3c] transition-all">
                        <span className="material-symbols-outlined text-sm">chevron_left</span>
                      </button>
                      <button className="w-8 h-8 rounded-lg border border-black/10 flex items-center justify-center text-[#595c5e] hover:bg-white hover:border-[#c5fe3c] transition-all">
                        <span className="material-symbols-outlined text-sm">chevron_right</span>
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </>
          )}

          {/* ══════════════════════════════════════════════════════════════
              TAB: STOCK REQUESTS
          ══════════════════════════════════════════════════════════════ */}
          {activeTab === "stock-requests" && (
            <div className="grid grid-cols-12 gap-8">
              {/* Request Form */}
              <div className="col-span-12 lg:col-span-4">
                <div className="bg-[#0c0f10] p-8 rounded-xl text-white shadow-xl shadow-black/5">
                  <h3 className="text-lg font-bold tracking-tight mb-6 flex items-center gap-2">
                    <span className="material-symbols-outlined text-[#c5fe3c]">add_shopping_cart</span>
                    Create Stock Request
                  </h3>
                  <div className="space-y-6">
                    <div>
                      <label className="block text-[10px] font-bold text-white/50 uppercase tracking-widest mb-2">Product Name</label>
                      <select value={reqIngredient} onChange={e => setReqIngredient(e.target.value)} className="w-full bg-white/10 border-none rounded-xl p-3 text-sm focus:ring-2 focus:ring-[#c5fe3c] outline-none text-white">
                        <option className="text-black" value="">Select product...</option>
                        {inventory.map(i => <option key={i.id} className="text-black" value={i.ingredientName}>{i.ingredientName}</option>)}
                      </select>
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold text-white/50 uppercase tracking-widest mb-2">Required Quantity</label>
                      <input value={reqQuantity} onChange={e => setReqQuantity(e.target.value === "" ? "" : Number(e.target.value))} className="w-full bg-white/10 border-none rounded-xl p-3 text-sm focus:ring-2 focus:ring-[#c5fe3c] outline-none text-white placeholder-white/30" placeholder="e.g., 50" type="number" />
                    </div>
                    <button onClick={handleStockRequest} disabled={isRequesting || !reqIngredient} className="w-full bg-[#c5fe3c] text-[#364b00] py-4 rounded-xl font-bold text-sm hover:-translate-y-0.5 transition-all shadow-[0_4px_14px_0_rgba(197,254,60,0.4)] disabled:opacity-50 disabled:hover:translate-y-0">
                      {isRequesting ? "Submitting..." : "Submit Request"}
                    </button>
                  </div>
                </div>
              </div>

              {/* Request History */}
              <div className="col-span-12 lg:col-span-8 flex flex-col">
                <div className="bg-white rounded-2xl shadow-sm border border-black/5 overflow-hidden flex-1">
                  <div className="p-6 border-b border-black/5 flex justify-between items-center bg-[#f5f6f8]">
                    <h3 className="text-lg font-bold tracking-tight">Request History</h3>
                  </div>
                  {loading ? (
                    <div className="p-12 text-center text-[#abadaf] text-sm">Loading requests...</div>
                  ) : stockRequests.length === 0 ? (
                    <div className="p-24 flex flex-col items-center justify-center text-center">
                      <div className="w-16 h-16 rounded-full bg-[#eff1f3] flex items-center justify-center mb-4">
                        <span className="material-symbols-outlined text-3xl text-[#abadaf]">history</span>
                      </div>
                      <p className="text-[#595c5e] font-semibold">No stock requests found.</p>
                      <p className="text-xs text-[#abadaf] mt-1">Submit your first request using the form.</p>
                    </div>
                  ) : (
                    <div className="overflow-x-auto">
                      <table className="w-full">
                        <thead className="bg-white border-b border-black/5">
                          <tr>
                            {["Request ID", "Product", "Quantity", "Status", "Date", "Action"].map(h => (
                              <th key={h} className="px-6 py-4 text-left text-[10px] font-black text-[#595c5e] uppercase tracking-widest">{h}</th>
                            ))}
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-black/5">
                          {stockRequests.map(req => (
                            <tr key={req.id} className="hover:bg-[#fafafa] transition-colors group">
                              <td className="px-6 py-5 text-sm font-bold text-[#0c0f10]">#{`REQ-${req.id}`}</td>
                              <td className="px-6 py-5">
                                <span className="font-semibold">{req.productName || req.ingredientName || "—"}</span>
                              </td>
                              <td className="px-6 py-5">
                                <span className="text-sm font-black">{req.requestedQuantity || req.quantity || "—"}</span>
                              </td>
                              <td className="px-6 py-5">
                                <span className={`inline-flex items-center px-2.5 py-1 rounded-md text-xs font-bold tracking-tight ${statusBadge(req.status)}`}>
                                  {req.status}
                                </span>
                              </td>
                              <td className="px-6 py-5 text-sm font-medium text-[#595c5e]">
                                {formatDate(req.createdAt || req.requestedAt)}
                              </td>
                              <td className="px-6 py-5 whitespace-nowrap text-right">
                                {req.status === 'DISPATCHED' && (
                                  <button
                                    onClick={() => handleReceiveRequest(req.id)}
                                    className="bg-[#c5fe3c] text-[#364b00] text-xs font-bold px-4 py-2 rounded-lg hover:-translate-y-0.5 transition-all shadow-sm"
                                  >
                                    Confirm Receipt
                                  </button>
                                )}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* ══════════════════════════════════════════════════════════════
              PLACEHOLDER TABS
          ══════════════════════════════════════════════════════════════ */}
          {!["dashboard", "inventory", "stock-requests"].includes(activeTab) && (
            <div className="flex flex-col items-center justify-center py-32 gap-4">
              <div className="w-20 h-20 rounded-2xl bg-[#eff1f3] flex items-center justify-center">
                <span className="material-symbols-outlined text-4xl text-[#abadaf]">
                  {navItems.find(n => n.id === activeTab)?.icon || "hourglass_empty"}
                </span>
              </div>
              <p className="text-lg font-bold text-[#0c0f10]">{pageTitle[activeTab]}</p>
              <p className="text-sm text-[#595c5e]">This section is coming soon.</p>
            </div>
          )}

        </div>
      </main>

      {/* FAB */}
      <button
        onClick={() => activeTab === "inventory" ? setShowAddModal(true) : setActiveTab("update-stock")}
        className="fixed bottom-8 right-8 w-14 h-14 bg-[#c5fe3c] text-[#364b00] rounded-full shadow-2xl flex items-center justify-center hover:-translate-y-1 transition-all z-50"
      >
        <span className="material-symbols-outlined text-3xl">add</span>
      </button>
    </div>
  );
}

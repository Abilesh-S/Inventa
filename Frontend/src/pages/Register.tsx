import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";

export default function Register() {
  const navigate = useNavigate();
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [role, setRole] = useState("staff");
  const [password, setPassword] = useState("");
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (password.length < 8) {
        setError("Password must be 8+ characters");
        return;
    }
    setError(null);
    setLoading(true);
    try {
      // Setup logic if needed, simulating for now or pointing to a hypothetical register endpoint
      // const res = await fetch(`${API_BASE}/users/register`, { ... })
      setTimeout(() => {
        alert("Registration requested. Please check your email or wait for approval.");
        navigate("/");
      }, 1000);
    } catch (err: any) {
      setError(err.message || "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-surface text-on-surface font-body min-h-screen flex flex-col w-full overflow-hidden">
      <main className="flex-grow flex flex-col md:flex-row min-h-screen">
        {/* Left Section with background Image/branding */}
        <section className="hidden md:flex md:w-5/12 bg-inverse-surface relative overflow-hidden p-12 flex-col justify-between">
          <div className="relative z-10">
            <div className="flex items-center gap-3 text-primary-fixed mb-12">
              <span className="material-symbols-outlined text-4xl" style={{ fontVariationSettings: "'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24" }}>inventory_2</span>
              <span className="text-3xl font-semibold tracking-tighter text-white">Ventorie</span>
            </div>
            <h1 className="text-5xl font-medium text-white tracking-tight leading-tight max-w-md">
              Kinetic inventory <span className="text-primary-fixed italic font-light">management</span> for modern enterprise.
            </h1>
            <p className="text-on-surface-variant mt-6 text-lg max-w-sm">
              Access real-time logistics data with an editorial-grade interface designed for velocity.
            </p>
          </div>

          <div className="relative z-10 space-y-6">
            <div className="glass-panel-dark p-6 rounded-xl border-l-4 border-primary-fixed">
              <p className="text-white font-medium italic">"The most intuitive logistics suite we've deployed this decade."</p>
              <div className="mt-4 flex items-center gap-3">
                <div className="w-10 h-10 rounded-full overflow-hidden bg-surface-container-high">
                  <img className="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuB_TNbBgVQb3wyVIzo98RbAvLh5BmxsJjOdgMwHNvDeeW18fwbL8HKYktpvKyZn2KhgSUhM_b6yCpFaaeuYL_wN5OfG5u2Z4SRjeZedMRdTGio6SSSeHe1XSycXgwEyMYXLAmyhvqJw_YEOVyYH-KVWLz3frtWC7XJh0YUtoYlUqbbB4o0kOVKpr5oV6l7Lf69xfHPCnAWl0ast9lif-UPJI0GoSqM5gkJ5Lf9ZV2IA1ovHpyu3W9lEEnvY3VY-SigtuqYcVa6RXa8" alt="Portrait" />
                </div>
                <div>
                  <p className="text-white text-sm font-semibold">Julian Pierce</p>
                  <p className="text-on-surface-variant text-xs">Director of Operations, AeroLogix</p>
                </div>
              </div>
            </div>
          </div>

          <div className="absolute top-0 right-0 w-full h-full opacity-20 pointer-events-none">
            <img className="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCBIDbPneuRpXxkJJNMueVbAnoP7UcGrjFuC_DmYOyCwSUdk8yom_uf-ztvvTktYdR5wL_M7A0M-NRsB9ioGwYY08wAgTQIIi5eMypTqOs1N76LKj6bHyo56XD8WGh2ahrNzbaEazbNkcdlssZboKk94z_MZhZZx0mu9HyaA-08ZZjk2QTjJ4iUeTT4QC9J8JR3gnPbBhWpTXyuif31TdEvrYhhjVYxoYnrGN1rJnaJCOtUZZwMfr9KoA3OyzsvXoc83QHCQ66xFw8" alt="Warehouse Background" />
          </div>
        </section>

        {/* Right Section form */}
        <section className="flex-grow w-full md:w-7/12 overflow-y-auto bg-surface p-6 md:p-12 lg:p-24 flex flex-col justify-center items-center">
          <div className="max-w-md w-full my-auto">
            <header className="mb-10">
              <h2 className="text-3xl font-medium tracking-tight text-on-surface mb-2">Create Account</h2>
              <p className="text-on-surface-variant">Join the next generation of logistics intelligence.</p>
            </header>

            <div className="space-y-6">
              {/* Social Logins */}
              <div className="grid grid-cols-2 gap-4">
                <button type="button" className="flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-surface-container-lowest text-on-surface font-medium border border-outline-variant/10 hover:bg-surface-container-low transition-all duration-300">
                  <img alt="Google" className="w-5 h-5" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAdWpBbojMcfTAOlZ0s1akkxkpnJSUkXJAO3YrA6dOCgveOkVpUVRKcaZX7R5oC9jaxDvBQ_VcWhDf5oyPVCS7DvJD20PPzCvYZkDAXxYHXL4P-JKv0UlNnHsP95cBtvotXNN68DDyZTMuHdmFTWLAFMzqOeUUIFMhZ5lNVsMmHjcEdRo8E1zNehm3FYYLwqLFgiqFwh4T1i5M8BVmuQVMOXfut6ulFytVx8O4DjXkSK614TKchCON0u_dNu_S0YVUPQ60Q8MVXlOY" />
                  <span>Google</span>
                </button>
                <button type="button" className="flex items-center justify-center gap-2 py-3 px-4 rounded-xl bg-surface-container-lowest text-on-surface font-medium border border-outline-variant/10 hover:bg-surface-container-low transition-all duration-300">
                  <span className="material-symbols-outlined text-xl" style={{ fontVariationSettings: "'FILL' 1, 'wght' 400, 'GRAD' 0, 'opsz' 24" }}>ios</span>
                  <span>Apple</span>
                </button>
              </div>

              <div className="flex items-center gap-4 text-outline-variant text-xs uppercase tracking-widest py-2">
                <div className="flex-grow h-px bg-outline-variant/20"></div>
                <span>or register with email</span>
                <div className="flex-grow h-px bg-outline-variant/20"></div>
              </div>

              <form onSubmit={handleSubmit} className="space-y-5">
                <div className="space-y-1">
                  <label className="block text-xs font-semibold text-on-surface-variant uppercase tracking-wider px-1" htmlFor="full_name">Full Name</label>
                  <input
                    id="full_name"
                    name="full_name"
                    type="text"
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    required
                    className="w-full px-4 py-3 bg-surface-container-low border-b border-transparent focus:border-primary focus:ring-0 transition-colors rounded-t-lg text-on-surface placeholder:text-outline/50"
                    placeholder="Alex Sterling"
                  />
                </div>

                <div className="space-y-1">
                  <label className="block text-xs font-semibold text-on-surface-variant uppercase tracking-wider px-1" htmlFor="email">Email Address</label>
                  <input
                    id="email"
                    name="email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    className="w-full px-4 py-3 bg-surface-container-low border-b border-transparent focus:border-primary focus:ring-0 transition-colors rounded-t-lg text-on-surface placeholder:text-outline/50"
                    placeholder="alex@enterprise.com"
                  />
                  {email === "" && (
                    <p className="text-error text-[11px] mt-1 flex items-center gap-1 opacity-60">
                      <span className="material-symbols-outlined text-xs" style={{ fontVariationSettings: "'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24" }}>error_outline</span>
                      Email is required
                    </p>
                  )}
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-1">
                    <label className="block text-xs font-semibold text-on-surface-variant uppercase tracking-wider px-1" htmlFor="phone">Phone Number</label>
                    <input
                      id="phone"
                      name="phone"
                      type="tel"
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      className="w-full px-4 py-3 bg-surface-container-low border-b border-transparent focus:border-primary focus:ring-0 transition-colors rounded-t-lg text-on-surface placeholder:text-outline/50"
                      placeholder="+1 (555) 000-0000"
                    />
                  </div>
                  <div className="space-y-1 relative">
                    <label className="block text-xs font-semibold text-on-surface-variant uppercase tracking-wider px-1" htmlFor="role">Organization Role</label>
                    <select
                      id="role"
                      name="role"
                      value={role}
                      onChange={(e) => setRole(e.target.value)}
                      className="w-full px-4 py-3 bg-surface-container-low border-b border-transparent focus:border-primary focus:ring-0 transition-colors rounded-t-lg text-on-surface appearance-none cursor-pointer"
                    >
                      <option value="staff">Staff</option>
                      <option value="manager">Manager</option>
                      <option value="admin">Admin</option>
                    </select>
                    <div className="absolute inset-y-0 right-3 flex items-center pt-5 pointer-events-none text-on-surface-variant">
                      <span className="material-symbols-outlined text-lg" style={{ fontVariationSettings: "'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24" }}>expand_more</span>
                    </div>
                  </div>
                </div>

                <div className="space-y-1">
                  <label className="block text-xs font-semibold text-on-surface-variant uppercase tracking-wider px-1" htmlFor="password">Password</label>
                  <div className="relative">
                    <input
                      id="password"
                      name="password"
                      type="password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      required
                      className="w-full px-4 py-3 bg-surface-container-low border-b border-transparent focus:border-primary focus:ring-0 transition-colors rounded-t-lg text-on-surface placeholder:text-outline/50"
                      placeholder="••••••••••••"
                    />
                    <button className="absolute right-3 top-1/2 -translate-y-1/2 text-on-surface-variant hover:text-primary transition-colors" type="button">
                      <span className="material-symbols-outlined text-lg" style={{ fontVariationSettings: "'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24" }}>visibility</span>
                    </button>
                  </div>
                  {error && <p className="text-error text-xs mt-1 flex items-center gap-1">
                      <span className="material-symbols-outlined text-sm" style={{ fontVariationSettings: "'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24" }}>error_outline</span>
                      {error}
                  </p>}
                </div>

                <div className="pt-4">
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full neon-gradient py-4 rounded-xl text-on-primary-fixed font-bold text-lg hover:shadow-lg hover:shadow-primary-container/40 transition-all duration-300 transform active:scale-[0.98]"
                  >
                    {loading ? "Creating..." : "Create Account"}
                  </button>
                </div>
              </form>

              <footer className="text-center pt-8">
                <p className="text-on-surface-variant">
                  Already have an account? <Link className="text-primary font-semibold hover:underline ml-1" to="/">Login here</Link>
                </p>
              </footer>
            </div>
          </div>
          
          {/* Mobile footer, visible on small screens when scrolling down */}
          <footer className="mt-8 md:hidden text-center text-xs text-neutral-500 pb-4">
             © 2024 Ventorie. Kinetic Inventory Management.
          </footer>
        </section>
      </main>
      
      {/* Footer Identity (Desktop absolute or flex) */}
      <footer className="hidden md:flex bg-transparent max-w-7xl w-full px-8 py-8 md:absolute md:bottom-0 md:right-0 xl:right-12 xl:max-w-4xl justify-between items-center z-50 pointer-events-none">
        <p className="font-['Inter'] text-sm tracking-normal text-neutral-500 pointer-events-auto">© 2024 Ventorie. Kinetic Inventory Management.</p>
        <nav className="flex gap-8 mt-4 md:mt-0 pointer-events-auto">
          <a className="font-['Inter'] text-sm text-neutral-500 hover:text-[#496400] transition-colors" href="#">Privacy</a>
          <a className="font-['Inter'] text-sm text-neutral-500 hover:text-[#496400] transition-colors" href="#">Terms</a>
          <a className="font-['Inter'] text-sm text-neutral-500 hover:text-[#496400] transition-colors" href="#">Support</a>
        </nav>
      </footer>
    </div>
  );
}

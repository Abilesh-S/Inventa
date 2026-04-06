import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";

const API_BASE = "http://localhost:8080/Inventa/api";

export default function Login() {
  const navigate = useNavigate();
  const [role, setRole] = useState("manager");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const endpoint = role === "owner" ? `${API_BASE}/users/login-owner` : `${API_BASE}/users/login`;
      const res = await fetch(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || "Login failed");
      }

      const user = await res.json();
      const auth = btoa(`${email}:${password}`);
      localStorage.setItem("user", JSON.stringify({ ...user, auth }));

      const userRole = user.role?.toUpperCase();
      if (userRole === "MANAGER") {
        navigate("/manager-dashboard");
      } else if (userRole === "STAFF") {
        navigate("/staff-dashboard");
      } else {
        navigate("/dashboard");
      }
    } catch (err: any) {
      setError(err.message || "Login failed. Please check your credentials.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-surface text-on-surface selection:bg-primary-container selection:text-on-primary-fixed min-h-screen flex flex-col relative w-full overflow-x-hidden">
      {/* Top Navigation Anchor */}
      <header className="w-full transition-all duration-300">
        <div className="flex justify-between items-center w-full px-6 py-5 max-w-7xl mx-auto">
          <div className="flex items-center gap-3">
            <span className="material-symbols-outlined text-primary text-3xl" style={{ fontVariationSettings: "'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24" }}>inventory_2</span>
            <h1 className="text-2xl font-bold tracking-tighter text-on-surface">Ventorie</h1>
          </div>
        </div>
      </header>

      {/* Main Content Canvas */}
      <main className="flex-grow flex items-center justify-center px-6 pb-12 z-10 relative">
        <div className="w-full max-w-[1200px] grid lg:grid-cols-2 gap-12 items-center">
          {/* Branding/Editorial Column */}
          <div className="hidden lg:block space-y-8">
            <div className="space-y-4">
              <span className="inline-flex px-3 py-1 rounded-full bg-primary-container text-on-primary-container text-xs font-bold tracking-widest uppercase">System 4.0</span>
              <h2 className="text-5xl font-medium tracking-tight text-on-surface leading-[1.1]">Inventory for the Modern Enterprise.</h2>
              <p className="text-on-surface-variant text-lg max-w-md">Precision analytics meets architectural design. Access your global vitals with a single secure entry.</p>
            </div>
            {/* Stat Card (Kinetic Minimalist) */}
            <div className="p-8 rounded-xl surface-container-lowest long-tail-shadow max-w-sm relative overflow-hidden bg-white">
              <div className="absolute top-0 right-0 p-4 opacity-10">
                <span className="material-symbols-outlined text-7xl" style={{ fontVariationSettings: "'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24" }}>analytics</span>
              </div>
              <div className="relative z-10">
                <p className="text-on-surface-variant text-sm font-medium mb-1">Global Throughput</p>
                <div className="flex items-baseline gap-2">
                  <span className="text-4xl font-bold tracking-tighter text-on-surface">94.2k</span>
                  <span className="px-2 py-0.5 rounded-full bg-primary-container text-on-primary-container text-[10px] font-bold">+12%</span>
                </div>
              </div>
            </div>
          </div>

          {/* Login Card Container */}
          <div className="w-full max-w-md mx-auto lg:mx-0">
            <div className="surface-container-lowest p-10 rounded-xl long-tail-shadow bg-white">
              <div className="mb-10">
                <h3 className="text-2xl font-semibold text-on-surface mb-2">Welcome Back</h3>
                <p className="text-on-surface-variant text-sm">Please enter your credentials to access the workspace.</p>
              </div>

              <form onSubmit={handleSubmit} className="space-y-6">
                {/* Role Selection */}
                <div className="space-y-2">
                  <label className="block text-[11px] font-bold uppercase tracking-wider text-on-surface-variant px-1">Access Role</label>
                  <div className="relative">
                    <select
                      value={role}
                      onChange={(e) => setRole(e.target.value)}
                      className="w-full appearance-none bg-surface-container-low border-none border-b-2 border-transparent focus:ring-0 focus:border-primary px-4 py-3.5 rounded-lg text-on-surface text-sm transition-all cursor-pointer"
                    >
                      <option value="owner">Owner</option>
                      <option value="manager">Manager</option>
                      <option value="staff">Staff</option>
                    </select>
                    <div className="absolute inset-y-0 right-3 flex items-center pointer-events-none text-on-surface-variant">
                      <span className="material-symbols-outlined text-xl" style={{ fontVariationSettings: "'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24" }}>expand_more</span>
                    </div>
                  </div>
                </div>

                {/* Email Input */}
                <div className="space-y-2 bg-transparent">
                  <label className="block text-[11px] font-bold uppercase tracking-wider text-on-surface-variant px-1">Email Address</label>
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    className="w-full bg-surface-container-low border-none border-b-2 border-transparent focus:ring-0 focus:border-primary px-4 py-3.5 rounded-lg text-on-surface text-sm placeholder:text-outline-variant transition-all"
                    placeholder="name@company.com"
                  />
                </div>

                {/* Password Input */}
                <div className="space-y-2 relative">
                  <div className="flex justify-between items-center px-1">
                    <label className="block text-[11px] font-bold uppercase tracking-wider text-on-surface-variant">Password</label>
                    <a className="text-[11px] font-bold uppercase tracking-wider text-primary hover:text-on-primary-fixed-variant transition-colors" href="#">Forgot?</a>
                  </div>
                  <div className="relative">
                    <input
                      type="password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      required
                      className="w-full bg-surface-container-low border-none border-b-2 border-transparent focus:ring-0 focus:border-primary px-4 py-3.5 rounded-lg text-on-surface text-sm placeholder:text-outline-variant transition-all"
                      placeholder="••••••••"
                    />
                    <button className="absolute inset-y-0 right-3 flex items-center text-on-surface-variant hover:text-on-surface" type="button">
                      <span className="material-symbols-outlined text-xl" style={{ fontVariationSettings: "'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 24" }}>visibility</span>
                    </button>
                  </div>
                </div>

                {error && <div className="text-error text-xs px-1">{error}</div>}

                {/* Submit Action */}
                <button
                  type="submit"
                  disabled={loading}
                  className="w-full py-4 neon-gradient-btn rounded-xl text-on-primary-fixed font-bold tracking-tight text-base hover:shadow-lg hover:shadow-primary/20 transform active:scale-[0.98] transition-all duration-200"
                >
                  {loading ? "Signing In..." : "Sign In"}
                </button>
              </form>

              {/* Divider */}
              <div className="my-8 flex items-center gap-4">
                <div className="h-[1px] flex-grow bg-outline-variant/20"></div>
                <span className="text-[10px] font-bold uppercase tracking-[0.2em] text-outline-variant">OR CONTINUE WITH</span>
                <div className="h-[1px] flex-grow bg-outline-variant/20"></div>
              </div>

              {/* Social Auth */}
              <div className="grid grid-cols-2 gap-4">
                <button type="button" className="flex items-center justify-center gap-2 py-3 rounded-lg border border-outline-variant/10 hover:bg-surface-container-low transition-colors duration-200">
                  <img alt="Google" className="w-4 h-4 opacity-80" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAcu-d0FCPy6cof--Iau8G30NM1fqwTFkAlym18Wo-ICO8wpbvEb0yO1xYg8SZPK0JZUnIoyE5ruhQiBKNb9Du_dRMhsm1B6gQ54GmfSLd1mFYNr0QRrIEFUObY5XAj2-6FVwPLwBRRo-HkrMhJ1FJPb3VO8n0EKtnOzB4UU8mvWxl4CNsI-AMtq8QFpccvxDbEzsswFVdSLqkweXRoZioRhW5ngJJst-R7e6CEM3jyePDGON5XDKb5yD9XDvzshbR7ypEc_bHKWr0" />
                  <span className="text-sm font-medium text-on-surface">Google</span>
                </button>
                <button type="button" className="flex items-center justify-center gap-2 py-3 rounded-lg border border-outline-variant/10 hover:bg-surface-container-low transition-colors duration-200">
                  <span className="material-symbols-outlined text-xl text-on-surface" style={{ fontVariationSettings: "'FILL' 1, 'wght' 400, 'GRAD' 0, 'opsz' 24" }}>ios</span>
                  <span className="text-sm font-medium text-on-surface">Apple</span>
                </button>
              </div>

              {/* Footer Link */}
              <p className="mt-10 text-center text-sm text-on-surface-variant">
                Don't have an account? <Link className="text-primary font-bold hover:underline decoration-2 underline-offset-4 ml-1" to="/register">Request Access</Link>
              </p>
            </div>
          </div>
        </div>
      </main>

      {/* Footer Identity */}
      <footer className="w-full mt-auto z-10 relative">
        <div className="flex flex-col md:flex-row justify-between items-center w-full px-8 py-8 max-w-7xl mx-auto space-y-4 md:space-y-0">
          <p className="font-['Inter'] text-sm tracking-normal text-neutral-500">© 2024 Ventorie. Kinetic Inventory Management.</p>
          <div className="flex gap-8">
            <a className="font-['Inter'] text-sm tracking-normal text-neutral-500 hover:text-primary transition-colors" href="#">Privacy</a>
            <a className="font-['Inter'] text-sm tracking-normal text-neutral-500 hover:text-primary transition-colors" href="#">Terms</a>
            <a className="font-['Inter'] text-sm tracking-normal text-neutral-500 hover:text-primary transition-colors" href="#">Support</a>
          </div>
        </div>
      </footer>

      {/* Decorative Background Elements */}
      <div className="absolute inset-0 w-full h-full -z-0 pointer-events-none overflow-hidden opacity-40">
        <div className="absolute top-[-10%] right-[-5%] w-[500px] h-[500px] rounded-full bg-primary-container/20 blur-[120px]"></div>
        <div className="absolute bottom-[-10%] left-[-5%] w-[400px] h-[400px] rounded-full bg-surface-container-high/30 blur-[100px]"></div>
      </div>
    </div>
  );
}

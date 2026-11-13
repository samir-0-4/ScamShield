import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { UserPlus, Mail, User, Lock, AlertCircle } from 'lucide-react';
import Card from '../components/common/Card';
import Button from '../components/common/Button';
import Input from '../components/ui/Input';
import { authService } from '../services/auth/authService';
import { getApiErrorMessage } from '../services/api/apiClient';

const validate = ({ fullName, email, password }) => {
  const errors = {};
  if (!fullName || fullName.trim().length < 2) errors.fullName = 'Full name must be at least 2 characters';
  if (!email) errors.email = 'Email is required';
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errors.email = 'Enter a valid email address';
  if (!password) errors.password = 'Password is required';
  else if (password.length < 8) errors.password = 'Password must be at least 8 characters';
  return errors;
};

export const RegisterPage = () => {
  const [fields, setFields] = useState({ fullName: '', email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const set = (k) => (e) => {
    setFields((f) => ({ ...f, [k]: e.target.value }));
    setErrors((er) => ({ ...er, [k]: '' }));
    setServerError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate(fields);
    if (Object.keys(errs).length) {
      setErrors(errs);
      return;
    }
    setLoading(true);
    try {
      await authService.register({
        fullName: fields.fullName,
        email: fields.email,
        password: fields.password,
      });
      navigate('/login?registered=1', { replace: true });
    } catch (err) {
      setServerError(getApiErrorMessage(err, 'Registration failed. The email may already be in use.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto pt-6 sm:pt-14 min-w-0">
      <div className="mb-7 space-y-2.5 select-none text-left">
        <div className="inline-flex p-3 rounded-2xl bg-gradient-to-br from-cyan-500/20 to-violet-500/20 border border-cyan-400/30 text-cyan-400 mb-1 shadow-[0_0_20px_rgba(59,130,246,0.25)]">
          <UserPlus className="w-6 h-6" />
        </div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">Create Account</h1>
        <p className="text-xs sm:text-sm text-slate-400">
          Join the PhantomTrace autonomous investigation network
        </p>
      </div>

      <Card className="shadow-[0_20px_50px_rgba(0,0,0,0.7),inset_0_1px_0_0_rgba(255,255,255,0.1)]">
        {serverError && (
          <div
            role="alert"
            className="mb-5 p-3.5 bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs sm:text-sm rounded-xl flex items-start gap-2.5 shadow-sm"
          >
            <AlertCircle className="w-4 h-4 text-rose-400 shrink-0 mt-0.5" />
            <span>{serverError}</span>
          </div>
        )}
        <form onSubmit={handleSubmit} className="space-y-4.5" noValidate>
          <Input
            label="Full name"
            type="text"
            required
            icon={User}
            value={fields.fullName}
            onChange={set('fullName')}
            placeholder="Analyst Name"
            error={errors.fullName}
            autoComplete="name"
          />
          <Input
            label="Email address"
            type="email"
            required
            icon={Mail}
            value={fields.email}
            onChange={set('email')}
            placeholder="analyst@phantomtrace.local"
            error={errors.email}
            autoComplete="email"
          />
          <Input
            label="Password"
            type="password"
            required
            icon={Lock}
            value={fields.password}
            onChange={set('password')}
            hint="Minimum 8 characters"
            error={errors.password}
            autoComplete="new-password"
          />
          <div className="pt-2">
            <Button type="submit" className="w-full font-semibold shadow-md" size="lg" loading={loading}>
              Create Account
            </Button>
          </div>
        </form>
        <div className="mt-6 pt-4 border-t border-white/[0.08] text-left text-xs text-slate-400 flex items-center justify-between">
          <span>Already registered?</span>
          <Link to="/login" className="text-cyan-400 hover:text-cyan-300 font-semibold transition-colors">
            Sign In &rarr;
          </Link>
        </div>
      </Card>
    </div>
  );
};

export default RegisterPage;

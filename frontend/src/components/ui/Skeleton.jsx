import { cn } from '../../utils/cn';

export const Skeleton = ({ className = '', ...props }) => {
  return (
    <div
      className={cn(
        'relative overflow-hidden rounded-lg bg-white/[0.04] border border-white/[0.04]',
        'after:absolute after:inset-0 after:-translate-x-full after:animate-shimmer after:bg-gradient-to-r after:from-transparent after:via-white/[0.08] after:to-transparent',
        className
      )}
      {...props}
    />
  );
};

export const CardSkeleton = ({ count = 1 }) => {
  return (
    <>
      {Array.from({ length: count }).map((_, idx) => (
        <div
          key={idx}
          className="rounded-2xl border border-white/[0.08] bg-[#0c101c]/90 p-5 space-y-4 shadow-card"
        >
          <div className="flex items-center justify-between">
            <Skeleton className="h-4 w-32" />
            <Skeleton className="h-5 w-16 rounded-full" />
          </div>
          <Skeleton className="h-3 w-48" />
          <div className="space-y-2 pt-2">
            <Skeleton className="h-2.5 w-full" />
            <Skeleton className="h-2.5 w-4/5" />
          </div>
        </div>
      ))}
    </>
  );
};

export const TableRowSkeleton = ({ rows = 5 }) => {
  return (
    <>
      {Array.from({ length: rows }).map((_, idx) => (
        <tr key={idx} className="border-b border-white/[0.06]">
          <td className="py-4 px-6">
            <Skeleton className="h-4 w-48" />
            <Skeleton className="h-3 w-24 mt-1.5" />
          </td>
          <td className="py-4 px-6">
            <Skeleton className="h-4 w-28" />
          </td>
          <td className="py-4 px-6">
            <Skeleton className="h-5 w-20 rounded-full" />
          </td>
          <td className="py-4 px-6">
            <Skeleton className="h-4 w-12" />
          </td>
          <td className="py-4 px-6 text-right">
            <Skeleton className="h-4 w-6 ml-auto" />
          </td>
        </tr>
      ))}
    </>
  );
};

export default Skeleton;

import Link from "next/link";
import { Avatar } from "@/components/atoms/Avatar";
import { Button, LinkButton } from "@/components/atoms/Button";
import { Logo } from "@/components/atoms/Logo";
import { Skeleton } from "@/components/atoms/Skeleton";
import { Copy } from "@/constants/copy";
import { Routes } from "@/constants/routes";
import type { User } from "@/types/entities/User";
import { classNames } from "@/utils/classNames";

interface HeaderBarProps {
  user: User | null;
  isLoading: boolean;
  currentPath: string;
  isSigningOut: boolean;
  onSignOut: () => void;
}

// No Attendee/Organiser switch: organiser is per event, so both views are links for everyone.
export function HeaderBar({ user, isLoading, currentPath, isSigningOut, onSignOut }: HeaderBarProps) {
  const links = [
    { href: Routes.home, label: Copy.nav.browse },
    ...(user ? [{ href: Routes.myRsvps, label: Copy.nav.myRsvps }, { href: Routes.dashboard, label: Copy.nav.dashboard }] : []),
  ];

  return (
    <header className="sticky top-0 z-10 border-b border-gray-200 bg-white/85 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-6xl items-center gap-4 px-4 sm:px-6">
        <Link href={Routes.home} aria-label={Copy.brand.homeLabel}>
          <Logo />
        </Link>
        <nav aria-label={Copy.nav.mainNavigation} className="flex flex-1 gap-1 overflow-x-auto">
          {links.map((link) => {
            const active = link.href === currentPath;
            return (
              <Link
                key={link.href}
                href={link.href}
                aria-current={active ? "page" : undefined}
                className={classNames(
                  "rounded-md px-3 py-1.5 text-label-m whitespace-nowrap transition-colors duration-150",
                  active ? "bg-gray-100 text-black" : "text-gray-500 hover:text-black",
                )}
              >
                {link.label}
              </Link>
            );
          })}
        </nav>
        <AccountArea user={user} isLoading={isLoading} isSigningOut={isSigningOut} onSignOut={onSignOut} />
      </div>
    </header>
  );
}

function AccountArea({ user, isLoading, isSigningOut, onSignOut }: Omit<HeaderBarProps, "currentPath">) {
  if (isLoading) {
    return <Skeleton className="h-8 w-24" />;
  }
  if (!user) {
    return (
      <div className="flex shrink-0 gap-2">
        <LinkButton href={Routes.signIn} variant="ghost" size="small">
          {Copy.nav.signIn}
        </LinkButton>
        <LinkButton href={Routes.signUp} variant="primary" size="small">
          {Copy.nav.signUp}
        </LinkButton>
      </div>
    );
  }
  return (
    <div className="flex shrink-0 items-center gap-2">
      <Button variant="ghost" size="small" disabled={isSigningOut} onClick={onSignOut}>
        {Copy.nav.signOut}
      </Button>
      <Avatar name={user.name} />
    </div>
  );
}

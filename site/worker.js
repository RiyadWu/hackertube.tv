// _redirects can't match on hostname or scheme, so www and plain http
// go to https://hackertube.tv here. Every other request is served from ./public.
export default {
  fetch(request, env) {
    const url = new URL(request.url);
    if (url.hostname === "www.hackertube.tv" || url.protocol === "http:") {
      url.hostname = "hackertube.tv";
      url.protocol = "https:";
      return Response.redirect(url, 301);
    }
    return env.ASSETS.fetch(request);
  },
};
